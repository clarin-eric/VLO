/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.importer.linkcheck.AvailabilityScoreAccumulator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import eu.clarin.cmdi.vlo.ResourceInfo;
import eu.clarin.cmdi.vlo.StringUtils;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.importer.linkcheck.ResourceAvailabilityStatusChecker;
import eu.clarin.cmdi.vlo.importer.normalizer.FormatPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.MultilingualPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.TemporalCoveragePostNormalizer;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStore;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Handles a single record in the import process
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @param <T>
 */
public class CMDIRecordImporter<T> {

    protected final static Logger LOG = LoggerFactory.getLogger(CMDIRecordImporter.class);
    private final AvailabilityScoreAccumulator availabilityScoreAccumulator = new AvailabilityScoreAccumulator();
    private final FieldNameServiceImpl fieldNameService;
    private final ResourceAvailabilityStatusChecker availabilityChecker;
    private final CMDIDataProcessor<T> processor;
    private final ImportStatistics stats;
    private final DocumentStore documentStore;
    private final DeduplicationSignature signature;
    private final ObjectMapper objectMapper;

    private final static DataRoot NOOP_DATAROOT = new DataRoot("dataroot", new File("/"), "http://null", "", false);

    /**
     * Contains MDSelflinks (usually). Just to know what we have already done.
     */
    private final Set<String> processedIds = Sets.newConcurrentHashSet();

    public CMDIRecordImporter(CMDIDataProcessor<T> processor, DocumentStore documentStore, FieldNameServiceImpl fieldNameService, ResourceAvailabilityStatusChecker availabilityChecker, ImportStatistics importStatistics, List<String> signatureFieldNames) {
        this.processor = processor;
        this.documentStore = documentStore;
        this.fieldNameService = fieldNameService;
        this.availabilityChecker = availabilityChecker;
        this.stats = importStatistics;
        this.signature = new DeduplicationSignature(signatureFieldNames);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Process single CMDI file with CMDIDataProcessor
     *
     * @param file CMDI input file
     * @param dataOrigin if left empty, a dummy data origin will be used to
     * populate the technical metadata
     * @param resourceStructureGraph leave empty skip hierarchy processing
     * @param endpointDescription if present, used to populate some fields
     * including national project
     * @throws eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException
     * @throws IOException
     */
    public void importRecord(File file, Optional<DataRoot> dataOrigin, Optional<ResourceStructureGraph> resourceStructureGraph, Optional<EndpointDescription> endpointDescription) throws DocumentStoreException, IOException {
        stats.nrOfFilesAnalyzed().incrementAndGet();
        CMDIData<T> cmdiData = null;
        try {
            cmdiData = processor.process(file, resourceStructureGraph.orElse(null));
            if (!idOk(cmdiData.getId())) {
                cmdiData.setId(
                        StringUtils.normalizeIdString(
                                dataOrigin.orElse(NOOP_DATAROOT).getOriginName()
                                + "/"
                                + file.getName())); //No id found in the metadata file so making one up based on the file name. Not quaranteed to be unique, but we have to set something.
                stats.nrOfFilesWithoutId().incrementAndGet();
            }
        } catch (Exception e) {
            LOG.error("error in file: {}", file, e);
            stats.nrOfFilesWithError().incrementAndGet();
        }
        if (cmdiData != null) {
            if (!cmdiData.hasResources()) {
                stats.nrOfFilesSkipped().incrementAndGet();
                LOG.warn("Skipping {}, no resource proxy found", file);
                return;
            }

            assert cmdiData.getId() != null; //idOk check guarantees this

            if (processedIds.add(cmdiData.getId())) {
                T document = cmdiData.getDocument();
                if (document != null) {
                    // add technical metadata
                    addTechnicalMetadata(file, cmdiData, dataOrigin.orElse(NOOP_DATAROOT), endpointDescription);
                    // add resource proxys      
                    addResourceData(cmdiData);
                    // update doc in store
                    submitDocumentUpdate(document, file);
                    // mark document as completed in graph
                    if (resourceStructureGraph.isPresent() && resourceStructureGraph.get().getVertex(cmdiData.getId()) != null) {
                        resourceStructureGraph.get().getVertex(cmdiData.getId()).setWasImported(true);
                    }
                }
            } else {
                stats.nrOfFilesSkipped().incrementAndGet();
                LOG.warn("Skipping {}, already processed id: {}", file, cmdiData.getId());
            }
        }
    }

    /**
     * Check id for validness
     *
     * @param id
     * @return true if id is acceptable, false otherwise
     */
    private boolean idOk(String id) {
        return id != null && !id.trim().isEmpty();
    }

    private void addTechnicalMetadata(File file, CMDIData<T> cmdiData, DataRoot dataOrigin, Optional<EndpointDescription> endpointDescription) {
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.HARVESTER_ROOT), dataOrigin.getOriginName(), false);
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.ID), cmdiData.getId(), false);
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.FILENAME), file.getAbsolutePath(), false);

        // data provided by CLARIN's OAI-PMH harvester
        endpointDescription.ifPresent(
                descr -> {
                    if (descr.getOaiEndpointUrl() != null) {
                        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.OAI_ENDPOINT_URI), descr.getOaiEndpointUrl(), false);
                    }
                    if (descr.getNationalProject() != null) {
                        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.NATIONAL_PROJECT), descr.getNationalProject(), false);
                    }
                    if (descr.getCentreName() != null) {
                        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER), descr.getCentreName(), false);
                    }
                }
        );
        if (!cmdiData.hasField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER))) {
            // use data root as substitute for dataProvider
            cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER), dataOrigin.getOriginName(), false);
        }

        String metadataSourceUrl = dataOrigin.getPrefix();
        metadataSourceUrl += file.getAbsolutePath().substring(dataOrigin.getToStrip().length());
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.COMPLETE_METADATA), metadataSourceUrl, false);

        // add SearchServices (should be CQL endpoint)
        cmdiData.getSearchResources().forEach((resource) -> {
            cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.SEARCH_SERVICE), resource.getResourceName(), false);
        });

        // add landing page resource
        final List<Resource> landingPageResources = cmdiData.getLandingPageResources();
        if (!landingPageResources.isEmpty()) {
            final Optional<Map<String, CheckedLink>> linkStatusForLandingPages = getLinkStatusForLandingPages(landingPageResources, file);
            landingPageResources.forEach((resource) -> {
                final String url = resource.getResourceName();
                if (url != null) {
                    final Optional<CheckedLink> landingPageStatus
                            = linkStatusForLandingPages.flatMap(s -> Optional.ofNullable(s.get(url)));
                    //create resource info object representation
                    final String landingPageValue = new ResourceInfo(
                            url,
                            resource.getMimeType(),
                            landingPageStatus.map(CheckedLink::getStatus).orElse(null),
                            landingPageStatus.map(CheckedLink::getTimestamp).map(Timestamp::getTime).orElse(null))
                            .toJson(objectMapper);
                    cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.LANDINGPAGE), landingPageValue, false);
                }
            });
        }

        // add search page resource
        cmdiData.getSearchPageResources().forEach((resource) -> {
            cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.SEARCHPAGE), resource.getResourceName(), false);
        });

        // add timestamp
        Date dt = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.LAST_SEEN), df.format(dt), false);

        // create and add document signature
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.SIGNATURE), signature.getSignature(cmdiData), false);

        // set number of days since last import to '0'
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.DAYS_SINCE_LAST_SEEN), 0, false);

        // set value for field languageCount based on the content of languageCode
        int languageCount;
        if (cmdiData.getDocField(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)) != null) {
            Collection<Object> docField = cmdiData.getDocField(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE));
            docField.remove("code:zxx");
            languageCount = docField.size();
            // special case "code:mul" (=one ISO 639-3 code for multiple languages)
            if (languageCount == 1 && docField.contains("code:mul")) {
                languageCount = 2; // =arbitrary number larger than 1
            }

            // consequences if field 'multilingual' has no concept-based content
            if (cmdiData.getDocField(fieldNameService.getFieldName(FieldKey.MULTILINGUAL)) == null) {
                if (languageCount > 1) {
                    cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.MULTILINGUAL), MultilingualPostNormalizer.VALUE_MULTILINGUAL, false);
                } else {
                    cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.MULTILINGUAL), MultilingualPostNormalizer.VALUE_NOT_MULTILINGUAL, false);
                }
            }
        } else {
            languageCount = 0;
        }

        // set temporalCoverage helper fields (start/end)
        Collection<Object> temporalCoverageField = cmdiData.getDocField(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE));
        if (temporalCoverageField != null && temporalCoverageField.size() == 1) {
            String tcValue = (String) temporalCoverageField.toArray()[0];
            Integer[] temporalRange = TemporalCoveragePostNormalizer.extractDateRange(tcValue);
            if(temporalRange != null) {
                cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE_START), temporalRange[0].toString() + "-01-01T00:00:00Z", false);
                cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.TEMPORAL_COVERAGE_END), temporalRange[1].toString() + "-12-31T23:59:59Z", false);
            }
        }

        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.LANGUAGE_COUNT), languageCount, false);
    }

    private Optional<Map<String, CheckedLink>> getLinkStatusForLandingPages(final List<Resource> landingPageResources, File file) {
        try {
            // get link status information
            return Optional.ofNullable(availabilityChecker.getLinkStatusForRefs(landingPageResources.stream().map(Resource::getResourceName)));
        } catch (Exception ex) {
            LOG.error("Error while checking resource availability for {}", file, ex);
            return Optional.empty();
        }
    }

    /**
     * Adds two fields FIELD_FORMAT and FIELD_RESOURCE. The Type can be
     * specified in the "ResourceType" element of an imdi file or possibly
     * overwritten by some more specific xpath (as in the LRT cmdi files). So if
     * a type is overwritten and already in the document we take that type.
     *
     * @param document
     * @param cmdiData
     */
    private void addResourceData(CMDIData cmdiData) {
        List<Object> fieldValues = cmdiData.hasField(fieldNameService.getFieldName(FieldKey.FORMAT))
                ? new ArrayList<>(cmdiData.getFieldValues(fieldNameService.getFieldName(FieldKey.FORMAT)))
                : null;
        cmdiData.removeField(fieldNameService.getFieldName(FieldKey.FORMAT)); //Remove old values they might be overwritten.
        final List<Resource> resources = cmdiData.getDataResources();
        final List<Resource> landingPages = cmdiData.getLandingPageResources();

        final Optional<Map<String, CheckedLink>> linkStatusMap = getLinkStatusForResources(resources, landingPages, cmdiData);

        for (int i = 0; i < resources.size(); i++) {
            final String fieldValue;
            if (fieldValues != null && i < fieldValues.size()) {
                fieldValue = fieldValues.get(i).toString();
            } else {
                fieldValue = null;
            }

            final ResourceInfo resourceInfo = createResourceInfo(linkStatusMap, resources.get(i), fieldValue);

            cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.RESOURCE), resourceInfo.toJson(objectMapper), false);

            // TODO check should probably be moved into Solr (by using some minimum length filter)
            if (!Strings.isNullOrEmpty(resourceInfo.getType())) {
                cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.FORMAT), resourceInfo.getType(), true);
            }
        }

        if (linkStatusMap.isPresent()) {
            final ResourceAvailabilityScore availabilityScore = availabilityScoreAccumulator.calculateAvailabilityScore(linkStatusMap.get(), resources.size() + landingPages.size());
            cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.RESOURCE_AVAILABILITY_SCORE), availabilityScore.getScoreValue(), false);
        }

        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.RESOURCE_COUNT), resources.size(), false);
    }
    
    private Optional<Map<String, CheckedLink>> getLinkStatusForResources(final List<Resource> resources, final List<Resource> landingPages, CMDIData cmdiData) {
        try {
            return Optional.ofNullable(availabilityChecker.getLinkStatusForRefs(
                    Streams
                            .concat(resources.stream(), landingPages.stream())
                            .map(Resource::getResourceName)));
        } catch (Exception ex) {
            LOG.error("Error while determining resource availability score for document {}", cmdiData.getId(), ex);
            return Optional.empty();
        }
    }

    private ResourceInfo createResourceInfo(final Optional<Map<String, CheckedLink>> linkStatusMap, Resource resource, String fieldValue) {
        // check link status
        final Optional<CheckedLink> linkStatus = linkStatusMap.flatMap(s -> Optional.ofNullable(s.get(resource.getResourceName())));

        // mime type value fallback chain
        final String mimeType = Optional.ofNullable(resource.getMimeType()) // prefer value from resource proxies
                .orElse(Optional.ofNullable(fieldValue) //fall back to value from format field
                        .orElse(linkStatus
                                .flatMap(s -> Optional.ofNullable(s.getContentType())) //fall back to value from link checker (if present)
                                .orElse(""))); // last resort

        // normalize mime type
        final String postProcessedMimeType = new FormatPostNormalizer().process(mimeType, null).get(0);

        return new ResourceInfo(resource.getResourceName(), postProcessedMimeType,
                linkStatus.map(CheckedLink::getStatus).orElse(null),
                linkStatus.map(CheckedLink::getTimestamp).map(Timestamp::getTime).orElse(null)
        );

    }

    /**
     * Adds some additional information from DataRoot to the document, add
     * document to document store
     *
     * @param document
     * @param file
     * @throws DocumentStoreException
     * @throws IOException
     */
    private void submitDocumentUpdate(T document, File file) throws DocumentStoreException,
            IOException {
        LOG.debug("Submitting to document store: {}", file);

        documentStore.addDocument(document);
        if (stats.nrOFDocumentsSent().incrementAndGet() % 250 == 0) {
            LOG.info("Number of documents sent thus far: {}", stats.nrOFDocumentsSent());
        }
    }
}
