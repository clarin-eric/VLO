package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * A list of facets. One FacetConfiguration for each facet.
 */
public class FacetMapping {

    private LinkedHashMap<String, FacetConfiguration> facets = new LinkedHashMap<>();

    public Collection<FacetConfiguration> getFacets() {
        return facets.values();
    }

    public void addFacet(FacetConfiguration facetConfiguration) {
        facets.put(facetConfiguration.getName(), facetConfiguration);
    }

    public FacetConfiguration getFacetConfiguration(String facetName) {
        return this.facets.computeIfAbsent(facetName, fn -> new FacetConfiguration(this, fn));
    }

    @Override
    public String toString() {
        return facets.toString();
    }

}
