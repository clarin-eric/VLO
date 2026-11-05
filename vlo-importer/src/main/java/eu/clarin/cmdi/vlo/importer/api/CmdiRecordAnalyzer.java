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
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Analyses CMDI files with VLO's facet mapping, without importing anything.
 *
 * <p>
 * Create one per run and reuse it: the facet mapping of each profile is built
 * on first use and kept for the lifetime of the analyzer.
 * </p>
 *
 * @param <T> document type of the analysed records
 */
public final class CmdiRecordAnalyzer<T> {

    private final AnalysisMappingFactory mappingFactory;
    private final CMDIRecordProcessor<T> processor;

    /**
     * @param vloConfig VLO configuration
     * @param strategy what the caller decides about the analysis
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if the strategy has no extra facets
     */
    public CmdiRecordAnalyzer(VloConfig vloConfig, ProcessingStrategy<T> strategy) {
        Objects.requireNonNull(vloConfig, "vloConfig");
        Objects.requireNonNull(strategy, "strategy");
        final Map<String, String> extraFacets = Objects.requireNonNull(strategy.extraFacets(), "extraFacets");
        if (extraFacets.isEmpty()) {
            throw new IllegalArgumentException("extraFacets must not be empty");
        }

        final FieldNameService fieldNames = new FieldNameServiceImpl(vloConfig);
        final VLOMarshaller marshaller = new VLOMarshaller();
        this.mappingFactory = new AnalysisMappingFactory(vloConfig, marshaller, strategy, extraFacets);

        final CMDIDataProcessor<T> parser = new CMDIParserVTDXML<>(
                MetadataImporter.registerPostProcessors(vloConfig, fieldNames, new LanguageCodeUtils(vloConfig)),
                MetadataImporter.registerPostMappingFilters(fieldNames),
                vloConfig,
                mappingFactory,
                marshaller,
                strategy.dataFactory(fieldNames),
                fieldNames,
                false);
        this.processor = strategy.recordProcessor(parser, fieldNames);
    }

    /**
     * Analyses one CMDI file.
     *
     * @return the record; empty if it was skipped or could not be processed
     */
    public Optional<CMDIData<T>> analyze(File file)
            throws DocumentStoreException, IOException {
        return processor.processRecord(file);
    }

    /**
     * The facet mapping for a profile, as {@link #analyze(File)} applies it,
     * including the extra facets.
     *
     * @param schemaLocation URL of the profile schema
     * @return the mapping; kept for reuse if the schema is remote
     */
    public FacetsMapping getFacetsMapping(String schemaLocation) {
        return mappingFactory.getFacetsMapping(Objects.requireNonNull(schemaLocation, "schemaLocation"));
    }
}
