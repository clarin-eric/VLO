package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of facets. One FacetConfiguration for each facet.
 */

public class FacetMapping {

    private List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>();

    public List<FacetConfiguration> getFacets() {
        return facets;
    }

    public void addFacet(FacetConfiguration facetConfiguration) {
        facets.add(facetConfiguration);
    }

}
