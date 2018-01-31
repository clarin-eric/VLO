package eu.clarin.cmdi.vlo.importer.mapping;

import eu.clarin.cmdi.vlo.importer.FacetConfiguration;

public class TargetFacet {
    private FacetConfiguration facetConfiguration;

    private boolean overrideExistingValues;
    private boolean removeSourceValue;
    private String value;

    public TargetFacet(FacetConfiguration facetConfiguration, String value) {
        this.removeSourceValue = true;
        this.facetConfiguration = facetConfiguration;
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
        this.overrideExistingValues = "true".equals(overrideExistingValues);
        this.removeSourceValue = !"false".equals(removeSourceValue); // should always be true if not set explicitly

    }

    public FacetConfiguration getFacetConfiguration() {
        return facetConfiguration;
    }

    public void setFacetConfiguration(FacetConfiguration facetConfiguration) {
        this.facetConfiguration = facetConfiguration;
    }

    public void setOverrideExistingValues(boolean overrideExistingValues) {
        this.overrideExistingValues = overrideExistingValues;
    }

    public boolean getOverrideExistingValues() {
        return this.overrideExistingValues;
    }

    public void setRemoveSourceValue(boolean removeSourceValue) {
        this.removeSourceValue = removeSourceValue;
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
