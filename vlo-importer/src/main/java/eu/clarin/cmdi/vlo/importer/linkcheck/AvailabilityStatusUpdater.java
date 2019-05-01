/*
 * Copyright (C) 2019 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.importer.linkcheck;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.rasa.links.CheckedLink;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import eu.clarin.cmdi.vlo.ResourceInfo;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.solr.SolrBridge;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityStatusUpdater {

    private final static Logger logger = LoggerFactory.getLogger(AvailabilityStatusUpdater.class);

    private final VloConfig config;
    private final SolrBridge solrBridge;
    private final ResourceAvailabilityStatusChecker statusChecker;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AvailabilityScoreAccumulator scoreAccumulator = new AvailabilityScoreAccumulator();

    private final String ID_FIELD;
    private final String RESOURCE_REF_FIELD;
    private final String RESOURCE_AVAILABILITY_SCORE_FIELD;

    public AvailabilityStatusUpdater(VloConfig config, SolrBridge solrBridge, ResourceAvailabilityStatusChecker statusChecker) {
        this.config = config;
        this.solrBridge = solrBridge;
        this.statusChecker = statusChecker;

        final FieldNameServiceImpl fieldNameService = new FieldNameServiceImpl(config);
        RESOURCE_REF_FIELD = fieldNameService.getFieldName(FieldKey.RESOURCE);
        RESOURCE_AVAILABILITY_SCORE_FIELD = fieldNameService.getFieldName(FieldKey.RESOURCE_AVAILABILITY_SCORE);
        ID_FIELD = fieldNameService.getFieldName(FieldKey.ID);

        logger.info("Updater instantiated with {} and {}", solrBridge.getClass(), statusChecker.getClass());
    }

    public void run() {
        try {
            logger.info("Initialising Solr connection");
            solrBridge.init();
        } catch (IOException ex) {
            logger.error("Failed to initialise Solr bridge", ex);
            return;
        }

        final SolrDocumentList recordList = getRecordList();
        logger.info("Query returned {} documents", recordList.getNumFound());

        //update all of the documents
        recordList.parallelStream().forEach(this::updateDoc);
    }

    private SolrDocumentList getRecordList() throws RuntimeException {
        final SolrQuery recordsQuery = new SolrQuery("*:*");
        recordsQuery.setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST);
        recordsQuery.setRows(Integer.MAX_VALUE);

        try {
            QueryResponse query = solrBridge.getClient().query(recordsQuery);
            return query.getResults();
        } catch (SolrServerException | IOException ex) {
            logger.error("Fatal exception while querying index", ex);
            throw new RuntimeException(ex);
        }
    }

    private void updateDoc(SolrDocument doc) {
        final Object docId = doc.getFieldValue(ID_FIELD);
        logger.debug("Current document: {}", docId);

        final Collection<Object> resourceRefValues = doc.getFieldValues(RESOURCE_REF_FIELD);
        if (resourceRefValues == null) {
            logger.debug("No resouce ref values in document {}", docId);
        } else {

            final Collection<ResourceInfo> resourceInfoObjects = resourceRefValues.parallelStream()
                    .filter(r -> (r instanceof String)) // filter out null and non-String values
                    .map(r -> ResourceInfo.fromJson(objectMapper, (String) r)) // deserialise
                    .filter(Objects::nonNull) //filter out failed deserialisations
                    .collect(Collectors.toSet());

            final Map<URI, CheckedLink> statusCheckResults = statusChecker.getLinkStatusForRefs(resourceInfoObjects.stream().map(ResourceInfo::getUrl));
            final AtomicInteger changes = new AtomicInteger(0);
            resourceInfoObjects.forEach((oldInfo) -> {
                try {
                    final URI targetUri = new URI(oldInfo.getUrl());
                    final CheckedLink checkResult = statusCheckResults.get(targetUri);

                    final ResourceInfo newInfo;
                    if (checkResult == null) {
                        if (oldInfo.getStatus() != null || oldInfo.getLastChecked() != null) {
                            logger.debug("Old info exists but no new info. Removing checking data from {}", oldInfo);
                            newInfo = new ResourceInfo(oldInfo.getUrl(), oldInfo.getType(), null, null);
                        } else {
                            logger.debug("Info did not change (did and does not exist) for {}", oldInfo);
                            newInfo = null;
                        }
                    } else {
                        if (!Objects.equals(checkResult.getStatus(), oldInfo.getStatus()) || !Objects.equals(checkResult.getTimestamp(), oldInfo.getLastChecked())) {
                            logger.debug("Info changed for {}", oldInfo);
                            //replace resource info with new
                            newInfo = new ResourceInfo(oldInfo.getUrl(), oldInfo.getType(), checkResult.getStatus(), checkResult.getTimestamp());
                        } else {
                            logger.debug("Info did not change for {}", oldInfo);
                            newInfo = null;
                        }
                    }
                    if (newInfo != null) {
                        final boolean success = doc.replace(RESOURCE_REF_FIELD, oldInfo.toJson(objectMapper), newInfo.toJson(objectMapper));
                        if (success) {
                            changes.incrementAndGet();
                            logger.debug("Successfully updated resource info for {}", docId);
                            logger.debug("Old info: {} => new info: {}", oldInfo, newInfo);
                        } else {
                            logger.error("Failed to replace info for {}. Old info: {} => new info: {}", docId, oldInfo, newInfo);
                        }
                    }
                } catch (URISyntaxException ex) {
                    logger.error("Cannot update status for URI: {}", ex.getMessage());
                }
            });

            if (changes.get() > 0) {
                logger.debug("Status changed. Calculate document score");
                final ResourceAvailabilityScore score = scoreAccumulator.calculateAvailabilityScore(statusCheckResults);
                doc.replace(RESOURCE_AVAILABILITY_SCORE_FIELD, score.getScoreValue());
            } else {
                logger.debug("Status not changed. Skipping calculation of document score");
            }
        }
    }

    public final static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Provide location of VloConfig.xml as parameter");
            System.exit(1);
        } else {
            File configFile = new File(args[0]);
            try {
                final VloConfig config = new XmlVloConfigFactory(configFile.toURI().toURL()).newConfig();
                final SolrBridge solrBridge = MetadataImporter.DefaultSolrBridgeFactory.createDefaultSolrBridge(config);
                final ResourceAvailabilityStatusChecker statusChecker = MetadataImporter.DefaultResourceAvailabilityFactory.createDefaultResourceAvailabilityStatusChecker(config);
                final AvailabilityStatusUpdater updater = new AvailabilityStatusUpdater(config, solrBridge, statusChecker);
                updater.run();
            } catch (IOException ex) {
                System.err.println("Could not read configuration file: " + ex.getMessage());
                System.exit(1);
            }
        }
    }

}
