package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * A list of facets. One FacetConfiguration for each facet.
 */

public class FacetMapping {

    private HashMap<String, FacetConfiguration> facets = new HashMap<String, FacetConfiguration>();

    public Collection<FacetConfiguration> getFacets() {
        return facets.values();
    }

    public void addFacet(FacetConfiguration facetConfiguration) {
        facets.put(facetConfiguration.getName(), facetConfiguration);
    }
    
    public FacetConfiguration getFacetConfiguration(String facetName){
    	return this.facets.computeIfAbsent(facetName, v -> new FacetConfiguration(this));
    }

}
