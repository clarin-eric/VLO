package eu.clarin.cmdi.vlo.importer;

import java.util.Map;

public class FacetMapping {
    
    private String idMapping;
    
    private Map<String, String> facetMap;

    public void setIdMapping(String idMapping) {
        this.idMapping = idMapping;
    }

    public String getIdMapping() {
        return idMapping;
    }

    public void setFacetMap(Map<String, String> facetMap) {
        this.facetMap = facetMap;
    }

    public Map<String, String> getFacetMap() {
        return facetMap;
    }

}
