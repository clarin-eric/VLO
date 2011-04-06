package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;

public class FacetMapping {

    private String idMapping;

    private List<FacetConfiguration> facets = new ArrayList<FacetConfiguration>();

    public void setIdMapping(String idMapping) {
        this.idMapping = idMapping;
    }

    public String getIdMapping() {
        return idMapping;
    }

    public void setFacets(List<FacetConfiguration> facets) {
        this.facets = facets;
    }

    public List<FacetConfiguration> getFacets() {
        return facets;
    }

    public void addFacet(FacetConfiguration facetConfiguration) {
        facets.add(facetConfiguration);
    }

}
