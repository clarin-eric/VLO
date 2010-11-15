package eu.clarin.cmdi.vlo.importer;

import java.io.File;

public class DataRoot {

    private FacetMapping facetMapping;
    private String originName;
    private File rootFile;

    public void setFacetMapping(FacetMapping facetMapping) {
        this.facetMapping = facetMapping;
    }

    public FacetMapping getFacetMapping() {
        return facetMapping;
    }

    public String getOriginName() {
        return originName;
    }
    
    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public File getRootFile() {
        return rootFile;
    }
    
    public void setRootFile(File rootFile) {
        this.rootFile = rootFile;
    }

}
