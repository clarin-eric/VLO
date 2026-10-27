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

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.ConceptLinkPathMapper;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * VLO's facet mapping factory, fed with the caller's concept links and extra
 * facets.
 *
 * <p>
 * The only class in this package that extends a VLO internal. Mappings are
 * built once per schema location and kept for the lifetime of the analyzer.
 * </p>
 */
class AnalysisMappingFactory extends FacetMappingFactory {

    private final VloConfig vloConfig;
    private final Function<String, Map<String, List<String>>> conceptPaths;
    private final Map<String, FacetDefinition> extraFacets;
    private final ConcurrentHashMap<String, FacetsMapping> mappings = new ConcurrentHashMap<>();

    AnalysisMappingFactory(VloConfig vloConfig, VLOMarshaller marshaller,
            Function<String, Map<String, List<String>>> conceptPaths, Map<String, String> extraFacets) {
        super(vloConfig, marshaller);
        this.vloConfig = vloConfig;
        this.conceptPaths = conceptPaths;
        this.extraFacets = toDefinitions(extraFacets);
    }

    /**
     * Called by VLO's parser for every record.
     */
    @Override
    public FacetsMapping getFacetMapping(String profileId, Boolean useLocalXSDCache) {
        return getFacetsMapping(vloConfig.getComponentRegistryProfileSchema(profileId));
    }

    FacetsMapping getFacetsMapping(String schemaLocation) {
        if (isRemote(schemaLocation)) {
            return mappings.computeIfAbsent(schemaLocation, this::build);
        }
        return build(schemaLocation);
    }

    private FacetsMapping build(String schemaLocation) {
        final Map<String, List<Pattern>> conceptLinkPaths = new HashMap<>();
        Objects.requireNonNullElse(conceptPaths.apply(schemaLocation), Map.<String, List<String>>of())
                .forEach((concept, xpaths) -> conceptLinkPaths.put(concept,
                        xpaths.stream().map(Pattern::new).collect(Collectors.toList())));

        final FacetsMapping mapping = createMapping(new ConceptLinkPathMapper() {
            @Override
            public Map<String, List<Pattern>> createConceptLinkPathMapping() {
                return conceptLinkPaths;
            }

            @Override
            public String getXsd() {
                return schemaLocation;
            }

            @Override
            public Boolean useLocalXSDCache() {
                // irrelevant: the concept links are supplied, so no XSD is fetched
                return true;
            }
        });
        return extraFacets.isEmpty() ? mapping : new FacetsMappingDecorator(mapping, extraFacets);
    }

    private static boolean isRemote(String schemaLocation) {
        return schemaLocation.startsWith("http://") || schemaLocation.startsWith("https://");
    }

    private static Map<String, FacetDefinition> toDefinitions(Map<String, String> extraFacets) {
        final Map<String, FacetDefinition> definitions = new LinkedHashMap<>();
        extraFacets.forEach((name, xpath) -> {
            final FacetDefinition definition = new FacetDefinition(null, name);
            definition.setPattern(new Pattern(xpath));
            definitions.put(name, definition);
        });
        return definitions;
    }
}
