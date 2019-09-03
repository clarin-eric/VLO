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
import com.google.common.collect.Streams;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AvailabilityStatusUpdater {

    private final static Logger logger = LoggerFactory.getLogger(AvailabilityStatusUpdater.class);

    public final static int SOLR_REQUEST_PAGE_SIZE = 250;

    private final SolrBridge solrBridge;
    private final ResourceAvailabilityStatusChecker statusChecker;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AvailabilityScoreAccumulator scoreAccumulator = new AvailabilityScoreAccumulator();

    private final String ID_FIELD;
    private final String RESOURCE_REF_FIELD;
    private final String LANDING_PAGE_FIELD;
    private final String RESOURCE_AVAILABILITY_SCORE_FIELD;

    private final AtomicInteger updateCount = new AtomicInteger();

    public AvailabilityStatusUpdater(VloConfig config, SolrBridge solrBridge, ResourceAvailabilityStatusChecker statusChecker) {
        this.solrBridge = solrBridge;
        this.statusChecker = statusChecker;

        final FieldNameServiceImpl fieldNameService = new FieldNameServiceImpl(config);
        RESOURCE_REF_FIELD = fieldNameService.getFieldName(FieldKey.RESOURCE);
        LANDING_PAGE_FIELD = fieldNameService.getFieldName(FieldKey.LANDINGPAGE);
        RESOURCE_AVAILABILITY_SCORE_FIELD = fieldNameService.getFieldName(FieldKey.RESOURCE_AVAILABILITY_SCORE);
        ID_FIELD = fieldNameService.getFieldName(FieldKey.ID);

        logger.info("Updater instantiated with {} and {}", solrBridge.getClass(), statusChecker.getClass());
    }

    public void run() {
        updateCount.set(0);

        // Initialise Solr bridge
        logger.info("Initialising Solr connection");
        initSolr();

        // Prepare Solr query
        final SolrQuery recordsQuery = new SolrQuery("*:*");
        recordsQuery.setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST);

        if (logger.isInfoEnabled()) {
            // Get result count
            recordsQuery.setRows(0);
            logger.info("Found {} documents", getSolrResult(recordsQuery).getResults().getNumFound());
        }

        // Prepare query for processing
        recordsQuery.setRows(SOLR_REQUEST_PAGE_SIZE);
        recordsQuery.setFields(ID_FIELD, RESOURCE_REF_FIELD, RESOURCE_AVAILABILITY_SCORE_FIELD, LANDING_PAGE_FIELD);
        recordsQuery.setSort(SolrQuery.SortClause.asc(ID_FIELD));

        // Loop over results, page by page
        final AtomicInteger seenCount = new AtomicInteger(0);
        queryAndProcess(recordsQuery, (response) -> {
            final SolrDocumentList result = response.getResults();

            final int pageSize = result.size();
            seenCount.addAndGet(pageSize);
            logger.debug("Records retrieved", pageSize);

            //update all of the documents (parallel stream)
            result.parallelStream().forEach(this::checkAndUpdateRecord);

            solrCommit();

            logger.info("Documents seen thus far: {}. Documents updated thus far: {}", seenCount, updateCount);
        });

        logger.info("All documents processed and committed. Shutting down Solr...");

        // Commit and tidy up
        shutdownSolr();

        logger.info("Done. Total number of documents updated: {}", updateCount);

    }

    public void queryAndProcess(final SolrQuery recordsQuery, Consumer<QueryResponse> resultPageConsumer) throws RuntimeException {
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;

        while (!done) {
            logger.debug("Request result page. Rows: {}; Cursor mark: {}", recordsQuery.getRows(), cursorMark);

            recordsQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            final QueryResponse response = getSolrResult(recordsQuery);

            resultPageConsumer.accept(response);

            done = (cursorMark.equals(response.getNextCursorMark()));
            cursorMark = response.getNextCursorMark();
        }
    }

    private QueryResponse getSolrResult(SolrQuery recordsQuery) throws RuntimeException {
        try {
            QueryResponse response = solrBridge.getClient().query(recordsQuery);
            return response;
        } catch (SolrServerException | IOException ex) {
            logger.error("Fatal exception while querying index", ex);
            throw new RuntimeException(ex);
        }
    }

    private void checkAndUpdateRecord(SolrDocument doc) {
        final Object docId = doc.getFieldValue(ID_FIELD);
        logger.debug("Current document: {}", docId);

        final Optional<Collection<Object>> resourceRefValues = Optional.ofNullable(doc.getFieldValues(RESOURCE_REF_FIELD));
        final Optional<Collection<Object>> landingPageValues = Optional.ofNullable(doc.getFieldValues(LANDING_PAGE_FIELD));

        if (!(resourceRefValues.isPresent() || landingPageValues.isPresent())) {
            logger.debug("No resouce ref values in document {}", docId);
        } else {
            //Resource refs
            final Collection<ResourceInfo> resourceInfoObjects = fieldToResourceInfos(resourceRefValues);
            //Landing pages refs
            final Collection<ResourceInfo> landingPageInfoObjects = fieldToResourceInfos(landingPageValues);

            //check links for both landing page and resource refs
            final Map<String, CheckedLink> statusCheckResults
                    = statusChecker.getLinkStatusForRefs(
                            Streams.concat(resourceInfoObjects.stream(), landingPageInfoObjects.stream())
                                    .map(ResourceInfo::getUrl)); //extract URLs

            final AtomicInteger changes = new AtomicInteger(0);
            final List<ResourceInfo> newResourceInfos = resourceInfoObjects
                    .stream()
                    .flatMap((info) -> updateResourceInfo(statusCheckResults, info, changes))
                    .collect(Collectors.toList());

            final List<ResourceInfo> newLandingPageInfos = landingPageInfoObjects
                    .stream()
                    .flatMap((info) -> updateResourceInfo(statusCheckResults, info, changes))
                    .collect(Collectors.toList());

            if (changes.get() > 0) {
                logger.debug("Link check status changed. Document will be updated.");

                //calculate new availability score
                logger.debug("Calculate document score");
                final int resourcesCount = Stream.of(resourceRefValues, landingPageValues)
                        .mapToInt(o -> o.map(Collection::size).orElse(0))
                        .sum();
                final ResourceAvailabilityScore score = scoreAccumulator.calculateAvailabilityScore(statusCheckResults, resourcesCount);

                final Integer currentScore = Optional.ofNullable(doc.getFieldValue(RESOURCE_AVAILABILITY_SCORE_FIELD)).filter(v -> (v instanceof Integer)).map(v -> (Integer) v).orElse(ResourceAvailabilityScore.UNKNOWN.getScoreValue());
                if (score.getScoreValue() != currentScore) {
                    changes.incrementAndGet();
                }

                //update document
                updateDocument(docId, score, newResourceInfos, newLandingPageInfos);
            } else {
                logger.debug("Status not changed. No need to update document.");
            }
        }
    }

    private Set<ResourceInfo> fieldToResourceInfos(final Optional<Collection<Object>> landingPageValues) {
        return landingPageValues
                .map(Collection::parallelStream)
                .orElse(Stream.empty())
                .filter(r -> (r instanceof String)) // filter out null and non-String values
                .map(r -> ResourceInfo.fromJson(objectMapper, (String) r)) // deserialise
                .filter(Objects::nonNull) //filter out failed deserialisations
                .collect(Collectors.toSet());
    }

    private Stream<ResourceInfo> updateResourceInfo(Map<String, CheckedLink> statusCheckResults, ResourceInfo oldInfo, AtomicInteger changes) {
        {
            final CheckedLink checkResult = statusCheckResults.get(oldInfo.getUrl());

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
        }
    }

    public void updateDocument(final Object docId, final ResourceAvailabilityScore score, final List<ResourceInfo> newResourceRefInfos, final List<ResourceInfo> newLandingPageInfos) {
        // serialize new resource ref values
        final List<String> newResourceRefValue = newResourceRefInfos.parallelStream()
                .map((info) -> info.toJson(objectMapper))
                .collect(Collectors.toList());
        final List<String> newLandingPageRefValue = newLandingPageInfos.parallelStream()
                .map((info) -> info.toJson(objectMapper))
                .collect(Collectors.toList());

        // make update document
        final SolrInputDocument solrInputDoc = new SolrInputDocument();
        solrInputDoc.setField(ID_FIELD, docId);
        solrInputDoc.setField(RESOURCE_REF_FIELD, ImmutableMap.of("set", newResourceRefValue));
        solrInputDoc.setField(LANDING_PAGE_FIELD, ImmutableMap.of("set", newLandingPageRefValue));
        solrInputDoc.setField(RESOURCE_AVAILABILITY_SCORE_FIELD, ImmutableMap.of("set", score.getScoreValue()));

        try {
            solrBridge.addDocument(solrInputDoc);
            updateCount.incrementAndGet();
        } catch (IOException | DocumentStoreException ex) {
            logger.error("Failed to store document to Solr", ex);
        }
    }

    private void initSolr() {
        try {
            solrBridge.init();
        } catch (IOException ex) {
            logger.error("Failed to initialise Solr bridge", ex);
            throw new RuntimeException("Error while trying to initialise Solr", ex);
        }
    }

    public void solrCommit() throws RuntimeException {
        try {
            logger.info("Committing to Solr");
            solrBridge.commit();
        } catch (IOException | SolrServerException ex) {
            throw new RuntimeException("Error while trying to commit to Solr index", ex);
        }
    }

    public void shutdownSolr() {
        try {
            solrCommit();
            solrBridge.shutdown();

        } catch (IOException | SolrServerException ex) {
            throw new RuntimeException("Error while trying to commit to Solr index", ex);
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
