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
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Analyses CMDI files with VLO's facet mapping, without importing anything.
 *
 * <p>
 * Create one per run and reuse it: the facet mapping of each profile is built
 * on first use and kept for the lifetime of the analyzer.
 * </p>
 */
public final class CmdiRecordAnalyzer {

    private final AnalysisMappingFactory mappingFactory;
    private final CMDIRecordProcessor<Map<String, List<ValueSet>>> processor;

    /**
     * @param vloConfig VLO configuration; supplies facet concepts, facets
     * configuration, value mappings and the Component Registry URL
     * @param conceptPaths for a profile schema location, the XPaths of each
     * concept link in that profile (concept URI to XPaths); called once per
     * profile
     * @param extraFacets facets to extract in addition to VLO's, as facet name
     * to XPath
     * @throws NullPointerException if any argument is {@code null}
     */
    public CmdiRecordAnalyzer(VloConfig vloConfig,
            Function<String, Map<String, List<String>>> conceptPaths,
            Map<String, String> extraFacets) {
        Objects.requireNonNull(vloConfig, "vloConfig");
        Objects.requireNonNull(conceptPaths, "conceptPaths");
        Objects.requireNonNull(extraFacets, "extraFacets");

        final FieldNameService fieldNames = new FieldNameServiceImpl(vloConfig);
        final VLOMarshaller marshaller = new VLOMarshaller();
        this.mappingFactory = new AnalysisMappingFactory(vloConfig, marshaller, conceptPaths, extraFacets);

        final CMDIDataProcessor<Map<String, List<ValueSet>>> parser = new CMDIParserVTDXML<>(
                MetadataImporter.registerPostProcessors(vloConfig, fieldNames, new LanguageCodeUtils(vloConfig)),
                MetadataImporter.registerPostMappingFilters(fieldNames),
                vloConfig,
                mappingFactory,
                marshaller,
                new ValueSetCmdiData.Factory(fieldNames),
                fieldNames,
                false);
        this.processor = new AnalysisRecordProcessor(parser, fieldNames);
    }

    /**
     * Analyses one CMDI file. No record is skipped for a
     * duplicate id or for having no resources.
     */
    public Optional<CMDIData<Map<String, List<ValueSet>>>> analyze(File file)
            throws DocumentStoreException, IOException {
        return processor.processRecord(file);
    }

    /**
     * The facet mapping for a profile, as {@link #analyze(File)} applies it,
     * including the extra facets.
     *
     * @param schemaLocation URL of the profile schema
     * @return the mapping; same instance on every call if a remote schema
     * location
     */
    public FacetsMapping getFacetsMapping(String schemaLocation) {
        return mappingFactory.getFacetsMapping(Objects.requireNonNull(schemaLocation, "schemaLocation"));
    }
}
