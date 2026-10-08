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
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds a {@link FacetsMapping} for a profile from caller-supplied concept
 * links.
 *
 * <p>
 * This is the supported way to apply VLO's facet-concept configuration to
 * concept links you resolved yourself.
 * </p>
 * <p>
 * Construction parses {@code facetConcepts.xml} and the facets configuration, so
 * build one instance and keep it. The builder holds no per-profile state and
 * does no caching — see {@link FacetsMappingProvider} for that.
 * </p>
 */
public class FacetsMappingBuilder {

    private final MappingFactory factory;

    /**
     * @param vloConfig VLO configuration supplying the facet concepts and facets
     * configuration files
     */
    public FacetsMappingBuilder(VloConfig vloConfig) {
        this(vloConfig, new VLOMarshaller());
    }

    /**
     * Exists for tests that need to supply their own marshaller. Embedders use
     * {@link #FacetsMappingBuilder(VloConfig)}.
     *
     * @param vloConfig VLO configuration supplying the facet concepts and facets
     * configuration files
     * @param marshaller marshaller used to read those files
     */
    FacetsMappingBuilder(VloConfig vloConfig, VLOMarshaller marshaller) {
        this.factory = new MappingFactory(
                Objects.requireNonNull(vloConfig, "vloConfig"),
                Objects.requireNonNull(marshaller, "marshaller"));
    }

    /**
     * Builds the mapping for one profile.
     *
     * @param schemaLocation URL of the CMDI profile schema
     * @param conceptLinkPaths concept URI to the XPath patterns
     * @return a new mapping; callers that want caching should hold the result
     * behind a {@link FacetsMappingProvider}
     */
    public FacetsMapping build(String schemaLocation, Map<String, List<Pattern>> conceptLinkPaths) {
        Objects.requireNonNull(schemaLocation, "schemaLocation");
        Objects.requireNonNull(conceptLinkPaths, "conceptLinkPaths");
        return factory.build(schemaLocation, conceptLinkPaths);
    }

    /**
     * The one place that still touches {@code protected createMapping}. Private
     * to have it as an implementation detail of this class, not a surface
     * embedders extend.
     */
    private static final class MappingFactory extends FacetMappingFactory {

        MappingFactory(VloConfig vloConfig, VLOMarshaller marshaller) {
            super(vloConfig, marshaller);
        }

        FacetsMapping build(String schemaLocation, Map<String, List<Pattern>> conceptLinkPaths) {
            return createMapping(new ConceptLinkPathMapper() {

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
                    // irrelevant: the caller resolved the concept links already,
                    // so no XSD is fetched on this path
                    return true;
                }
            });
        }
    }
}
