/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.vlo.importer.api;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Analyses a single CMDI file: applies VLO's facet mapping and returns the
 * resulting record, without importing anything.
 */
@FunctionalInterface
public interface CmdiRecordAnalyzer {

    /**
     * Analyses one CMDI file.
     *
     * @param file the CMDI record
     * @return the analysed record, or empty if the processor skipped the file
     * @throws DocumentStoreException if the record could not be turned into a
     * document
     * @throws IOException if the file could not be read
     */
    Optional<CMDIData<Map<String, List<ValueSet>>>> analyze(File file)
            throws DocumentStoreException, IOException;

    /**
     * Creates an analyzer.
     *
     * <p>
     * The provider is the only setting. Everything else is fixed for analysis
     * rather than import: no record is skipped, and values are kept as full
     * {@link ValueSet}s.
     * </p>
     *
     * @param vloConfig VLO configuration; supplies facet concepts, facets
     * configuration and profile schema resolution
     * @param facetsMappingProvider resolves a profile schema location to the
     * mapping to apply, and is the natural place to cache
     * @return a new analyzer
     * @throws NullPointerException if either argument is {@code null}
     */
    static CmdiRecordAnalyzer create(VloConfig vloConfig, FacetsMappingProvider facetsMappingProvider) {
        Objects.requireNonNull(vloConfig, "vloConfig");
        Objects.requireNonNull(facetsMappingProvider, "facetsMappingProvider");

        final FieldNameService fieldNames = new FieldNameServiceImpl(vloConfig);
        final CMDIDataFactory<Map<String, List<ValueSet>>> dataFactory
                = new ValueSetCmdiData.Factory(fieldNames);
        final VLOMarshaller vloMarshaller = new VLOMarshaller();

        final CMDIDataProcessor<Map<String, List<ValueSet>>> dataProcessor = new CMDIParserVTDXML<>(
                MetadataImporter.registerPostProcessors(vloConfig, fieldNames, new LanguageCodeUtils(vloConfig)),
                MetadataImporter.registerPostMappingFilters(fieldNames),
                vloConfig,
                new ProviderBackedMappingFactory(vloConfig, vloMarshaller, facetsMappingProvider),
                vloMarshaller,
                dataFactory,
                fieldNames,
                false);

        final CMDIRecordProcessor<Map<String, List<ValueSet>>> processor
                = new AnalysisRecordProcessor(dataProcessor, fieldNames);

        return processor::processRecord;
    }
}
