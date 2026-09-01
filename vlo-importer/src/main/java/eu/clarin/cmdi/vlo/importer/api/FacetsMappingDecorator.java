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

import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@link FacetsMapping} that adds extra facet definitions on top of another
 * mapping.
 *
 */
public class FacetsMappingDecorator extends FacetsMapping {

    @Serial
    private static final long serialVersionUID = 1L;

    private final FacetsMapping delegate;
    private final LinkedHashMap<String, FacetDefinition> additional;

    /**
     * @param delegate the mapping to extend
     * @param additionalFacets extra definitions, keyed by facet name; iteration
     * order is preserved
     */
    public FacetsMappingDecorator(FacetsMapping delegate, Map<String, FacetDefinition> additionalFacets) {
        super(Objects.requireNonNull(delegate, "delegate").getFacetsConfigurations());
        this.delegate = delegate;
        this.additional = new LinkedHashMap<>(Objects.requireNonNull(additionalFacets, "additionalFacets"));
    }

    @Override
    public Collection<FacetDefinition> getFacetDefinitions() {
        final Collection<FacetDefinition> union = new ArrayList<>(delegate.getFacetDefinitions());
        union.addAll(additional.values());
        return union;
    }

    @Override
    public Collection<String> getFacetConfigurationNames() {
        final Collection<String> union = new ArrayList<>(delegate.getFacetConfigurationNames());
        union.addAll(additional.keySet());
        return union;
    }

    @Override
    public FacetDefinition getFacetDefinition(String facetName) {
        final FacetDefinition extra = additional.get(facetName);
        return extra != null ? extra : delegate.getFacetDefinition(facetName);
    }

    @Override
    public String toString() {
        return "FacetsMappingDecorator{additional=" + additional.keySet() + ", delegate=" + delegate + '}';
    }
}
