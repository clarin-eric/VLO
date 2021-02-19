/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that provides information about the facets configuration.
 * {@link #init()} method must be called!
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 * @see FacetsConfiguration
 */
public class FacetConfigurationServiceImpl implements FacetConfigurationService {

    public static class DisplayAs {

        private static final String SEARCH_RESULT_FIELD = "searchResultField";
        private static final String TECHNICAL_FIELD = "technicalField";
        private static final String IGNORED_FIELD = "ignoredField";
        private static final String SECONDARY_FACET = "secondaryFacet";
        private static final String PRIMARY_FACET = "primaryFacet";
    }

    private final static Logger logger = LoggerFactory.getLogger(FacetConfigurationServiceImpl.class);

    private Map<String, Facet> facets = Collections.emptyMap();
    private final FacetsConfiguration facetsConfiguration;
    private List<String> facetFields;
    private List<String> primaryFacetFields;
    private List<String> ignoredFields;
    private List<String> technicalFields;
    private List<String> searchResultFields;

    public FacetConfigurationServiceImpl(FacetsConfiguration facetsConfiguration) {
        this.facetsConfiguration = facetsConfiguration;
    }

    public void init() {
        facets = facetsConfiguration
                .getFacet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(Facet::getName, Function.identity()));

        primaryFacetFields = getFilteredFacetNames(hasDisplayProperties(DisplayAs.PRIMARY_FACET))
                .collect(Collectors.toUnmodifiableList());
        facetFields = Streams.concat(
                getFilteredFacetNames(hasDisplayProperties(DisplayAs.PRIMARY_FACET)),
                getFilteredFacetNames(hasDisplayProperties(DisplayAs.SECONDARY_FACET)))
                .collect(Collectors.toUnmodifiableList());
        ignoredFields = getFilteredFacetNames(hasDisplayProperties(DisplayAs.IGNORED_FIELD))
                .collect(Collectors.toUnmodifiableList());
        technicalFields = getFilteredFacetNames(hasDisplayProperties(DisplayAs.TECHNICAL_FIELD))
                .collect(Collectors.toUnmodifiableList());
        searchResultFields = getFilteredFacetNames(hasDisplayProperties(DisplayAs.SEARCH_RESULT_FIELD))
                .collect(Collectors.toUnmodifiableList());
    }

    private Stream<String> getFilteredFacetNames(Predicate<Facet> predicate) {
        return facets.values().stream()
                .filter(predicate)
                .map(Facet::getName);
    }

    private Predicate<Facet> hasDisplayProperties(String... properties) {
        return facet -> Stream.of(properties).allMatch(property -> facet.getDisplayAs().contains(property));
    }

    @Override
    public String getDescription(String facetName) {
        return Optional.ofNullable(facets.get(facetName))
                .map(Facet::getDescription)
                .orElse(null);
    }

    @Override
    public List<String> getFacetFieldNames() {
        return facetFields;
    }

    @Override
    public Collection<String> getIgnoredFieldNames() {
        return ignoredFields;
    }

    @Override
    public Collection<String> getTechnicalFieldNames() {
        return technicalFields;
    }

    @Override
    public Collection<String> getSearchResultFieldNames() {
        return searchResultFields;
    }

    @Override
    public Collection<String> getPrimaryFacetFieldNames() {
        return primaryFacetFields;
    }

    /**
     *
     * @return all facet fields, including collection facet (arbitrary order
     * unspecified)
     * @deprecated Use {@link #getFacetFieldNames() }
     * @see #getFacetFieldNames()
     */
    @Override
    @Deprecated
    public List<String> getFacetsInSearch() {
        return getFacetFieldNames();
    }
}
