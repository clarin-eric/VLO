/*
 * Copyright (C) 2023 CLARIN
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

import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import eu.clarin.cmdi.vlo.config.DataRoot;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.importer.linkcheck.AvailabilityScoreAccumulator;
import eu.clarin.cmdi.vlo.importer.linkcheck.LinkStatus;
import eu.clarin.cmdi.vlo.importer.linkcheck.ResourceAvailabilityStatusChecker;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStore;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports a single record into the provided document store
 *
 * @author twagoo
 */
public class CMDIRecordImporter<T> extends CMDIRecordProcessor<T> {

    protected final static Logger LOG = LoggerFactory.getLogger(CMDIRecordImporter.class);

    private final AvailabilityScoreAccumulator availabilityScoreAccumulator = new AvailabilityScoreAccumulator();
    private final ResourceAvailabilityStatusChecker availabilityChecker;
    private final ImportStatistics stats;
    private final DeduplicationSignature signature;
    private final DocumentStore documentStore;
    private final FieldNameServiceImpl fieldNameService;

    private final CMDIRecordProcessorListener processorListener = new CMDIRecordProcessorListener() {
        @Override
        public void handleFileWithoutId(File file) {
            LOG.debug("File without ID: {}", file);
            stats.nrOfFilesWithoutId().incrementAndGet();
        }

        @Override
        public void handleErrorInFile(File file, Exception e) {
            LOG.error("error in file: {}", file, e);
            stats.nrOfFilesWithError().incrementAndGet();
        }

        @Override
        public void handleFileSkipped(File file, String reason) {
            stats.nrOfFilesSkipped().incrementAndGet();
            LOG.warn("Skipping {}: {}", file, reason);
        }
    };

    public CMDIRecordImporter(CMDIDataProcessor<T> processor, DocumentStore documentStore, FieldNameServiceImpl fieldNameService, ResourceAvailabilityStatusChecker availabilityChecker, ImportStatistics stats, List<String> signatureFields) {
        super(processor, fieldNameService);
        this.fieldNameService = fieldNameService;
        this.availabilityChecker = availabilityChecker;
        this.stats = stats;
        this.signature = new DeduplicationSignature(signatureFields);
        this.documentStore = documentStore;
        setProcessingListener(processorListener);
    }

    /**
     * Import a single CMDI file
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
        final Optional<CMDIData<T>> result = super.processRecord(file, dataOrigin, resourceStructureGraph, endpointDescription);
        if (result.isPresent()) {
            final CMDIData<T> cmdiData = result.get();
            // update doc in store
            submitDocumentUpdate(cmdiData.getDocument(), file);
            // mark document as completed in graph
            if (resourceStructureGraph.isPresent() && resourceStructureGraph.get().getVertex(cmdiData.getId()) != null) {
                resourceStructureGraph.get().getVertex(cmdiData.getId()).setWasImported(true);
            }
        } else {
            LOG.info("Record not imported: {}", file);
        }
    }

    @Override
    protected void addTechnicalMetadata(File file, CMDIData<T> cmdiData, DataRoot dataOrigin, Optional<EndpointDescription> endpointDescription) {
        super.addTechnicalMetadata(file, cmdiData, dataOrigin, endpointDescription);

        // create and add document signature
        cmdiData.addDocField(fieldNameService.getFieldName(FieldKey.SIGNATURE), signature.getSignature(cmdiData), false);
    }

    @Override
    protected Optional<Map<String, LinkStatus>> getLinkStatusForLandingPages(final List<Resource> landingPageResources, File file) {
        try {
            // get link status information
            return Optional.ofNullable(availabilityChecker.getLinkStatusForRefs(landingPageResources.stream().map(Resource::getResourceName)));
        } catch (Exception ex) {
            LOG.error("Error while checking resource availability for {}", file, ex);
            return Optional.empty();
        }
    }

    @Override
    protected Optional<Map<String, LinkStatus>> getLinkStatusForResources(final List<Resource> resources, final List<Resource> landingPages, CMDIData cmdiData) {
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

    @Override
    protected Optional<ResourceAvailabilityScore> calculateAvailabilityScore(final Optional<Map<String, LinkStatus>> linkStatusMap, final List<Resource> resources, final List<Resource> landingPages) {
        final ResourceAvailabilityScore availabilityScore = availabilityScoreAccumulator.calculateAvailabilityScore(linkStatusMap.get(), resources.size() + landingPages.size());
        return Optional.of(availabilityScore);
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

    @Override
    protected boolean skipOnNoResources() {
        return true;
    }

    @Override
    protected boolean skipOnDuplicateId() {
        return true;
    }

}
