package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.Serializable;

public class TargetFacet implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private FacetConfiguration facetConfiguration;

    private boolean overrideExistingValues;
    private boolean removeSourceValue;
    private String value;

    public TargetFacet(FacetConfiguration facetConfiguration, String value) {
        this.facetConfiguration = facetConfiguration;
        this.overrideExistingValues = false;
        this.removeSourceValue = true;
        this.value = value;
    }

    public TargetFacet(TargetFacet targetFacet) {
        this.facetConfiguration = targetFacet.facetConfiguration;
        this.overrideExistingValues = targetFacet.overrideExistingValues;
        this.removeSourceValue = targetFacet.removeSourceValue;
        this.value = targetFacet.value;
    }

    public TargetFacet(FacetConfiguration facetConfiguration, String overrideExistingValues, String removeSourceValue) {
        this.facetConfiguration = facetConfiguration;
        setOverrideExistingValues(overrideExistingValues);
        setRemoveSourceValue(removeSourceValue);

    }
    
    public TargetFacet(FacetConfiguration facetConfiguration, String overrideExistingValues, String removeSourceValue, String value) {
        this.facetConfiguration = facetConfiguration;
        setOverrideExistingValues(overrideExistingValues);
        setRemoveSourceValue(removeSourceValue);
        this.value = value;
    }

    public FacetConfiguration getFacetConfiguration() {
        return facetConfiguration;
    }

    public void setFacetConfiguration(FacetConfiguration facetConfiguration) {
        this.facetConfiguration = facetConfiguration;
    }

    public void setOverrideExistingValues(String overrideExistingValues) {
        this.overrideExistingValues = "true".equalsIgnoreCase(overrideExistingValues); // should always be false if not set explicitly true
    }

    public boolean getOverrideExistingValues() {
        return this.overrideExistingValues;
    }

    public void setRemoveSourceValue(String removeSourceValue) {
        this.removeSourceValue = "true".equalsIgnoreCase(removeSourceValue); // should always be false if not set explicitly true
    }

    public boolean getRemoveSourceValue() {
        return this.removeSourceValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TargetFacet clone() {
        return new TargetFacet(this);
    }
}
