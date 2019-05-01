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
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.rasa.links.CheckedLink;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import eu.clarin.cmdi.vlo.ResourceInfo;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import eu.clarin.cmdi.vlo.importer.solr.SolrBridge;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityStatusUpdater {

    private final static Logger logger = LoggerFactory.getLogger(AvailabilityStatusUpdater.class);

    private final SolrBridge solrBridge;
    private final ResourceAvailabilityStatusChecker statusChecker;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AvailabilityScoreAccumulator scoreAccumulator = new AvailabilityScoreAccumulator();

    private final String ID_FIELD;
    private final String RESOURCE_REF_FIELD;
    private final String RESOURCE_AVAILABILITY_SCORE_FIELD;

    private final AtomicInteger updateCount = new AtomicInteger();

    public AvailabilityStatusUpdater(VloConfig config, SolrBridge solrBridge, ResourceAvailabilityStatusChecker statusChecker) {
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

        updateCount.set(0);

        //update all of the documents (parallel stream)
        recordList.parallelStream().forEach(this::checkAndUpdateRecord);

        try {
            logger.info("Committing to Solr");
            solrBridge.commit();
            solrBridge.shutdown();

            logger.info("Done. Total number of documents updated: {}", updateCount);

        } catch (IOException | SolrServerException ex) {
            logger.error("Failed to commit to Solr", ex);
        }

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

    private void checkAndUpdateRecord(SolrDocument doc) {
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
            final List<ResourceInfo> newInfos = resourceInfoObjects
                    .stream()
                    .flatMap((info) -> updateResourceInfo(statusCheckResults, info, changes))
                    .collect(Collectors.toList());

            if (changes.get() > 0) {
                //calculate new availability score
                logger.debug("Link check status changed. Calculate document score");
                final ResourceAvailabilityScore score = scoreAccumulator.calculateAvailabilityScore(statusCheckResults);

                //update document
                updateDocument(docId, score, newInfos);
            } else {
                logger.debug("Status not changed. Skipping calculation of document score");
            }
        }
    }

    private Stream<ResourceInfo> updateResourceInfo(Map<URI, CheckedLink> statusCheckResults, ResourceInfo oldInfo, AtomicInteger changes) {
        {
            try {
                final URI targetUri = new URI(oldInfo.getUrl());
                final CheckedLink checkResult = statusCheckResults.get(targetUri);

                if (checkResult == null) {
                    if (oldInfo.getStatus() != null || oldInfo.getLastChecked() != null) {
                        final ResourceInfo newInfo = new ResourceInfo(oldInfo.getUrl(), oldInfo.getType(), null, null);

                        logger.info("Old info exists but no new info. Removing link checking properties from {} => {}", oldInfo, newInfo);
                        changes.incrementAndGet();
                        return Stream.of(newInfo);
                    } else {
                        logger.debug("Info did not change (did and does not exist) for {}", oldInfo);
                        return Stream.of(oldInfo);
                    }
                } else {
                    if (!Objects.equals(checkResult.getStatus(), oldInfo.getStatus()) || !Objects.equals(checkResult.getTimestamp(), oldInfo.getLastChecked())) {
                        final ResourceInfo newInfo = new ResourceInfo(oldInfo.getUrl(), oldInfo.getType(), checkResult.getStatus(), checkResult.getTimestamp());

                        logger.info("Info changed for {} => {}", oldInfo, newInfo);
                        changes.incrementAndGet();
                        return Stream.of(newInfo);
                    } else {
                        logger.debug("Info did not change for {}", oldInfo);
                        return Stream.of(oldInfo);
                    }
                }
            } catch (URISyntaxException ex) {
                logger.error("Cannot update status for URI: {}", ex.getMessage());
                return Stream.of(oldInfo);
            }
        }
    }

    public void updateDocument(final Object docId, final ResourceAvailabilityScore score, final List<ResourceInfo> newInfos) {
        // serialize new resource ref values
        final List<String> newValue = newInfos.parallelStream()
                .map((info) -> info.toJson(objectMapper))
                .collect(Collectors.toList());

        // make update document
        final SolrInputDocument solrInputDoc = new SolrInputDocument();
        solrInputDoc.setField(ID_FIELD, docId);
        solrInputDoc.setField(RESOURCE_REF_FIELD, ImmutableMap.of("set", newValue));
        solrInputDoc.setField(RESOURCE_AVAILABILITY_SCORE_FIELD, ImmutableMap.of("set", score.getScoreValue()));

        try {
            solrBridge.addDocument(solrInputDoc);
            updateCount.incrementAndGet();
        } catch (IOException | DocumentStoreException ex) {
            logger.error("Failed to store document to Solr", ex);
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
