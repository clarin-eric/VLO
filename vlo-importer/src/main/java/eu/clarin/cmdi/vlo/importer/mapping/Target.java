package eu.clarin.cmdi.vlo.importer.mapping;

import eu.clarin.cmdi.vlo.importer.FacetConfiguration;

public class Target {
	private FacetConfiguration facetConfiguration;
	
	private boolean overrideExistingValues;
	private boolean removeSourceValue;
	private String value;
	
	
	public Target(FacetConfiguration facetConfiguration, String value) {
		super();
		this.facetConfiguration = facetConfiguration;
		this.value = value;
	}
	
	public Target(FacetConfiguration facetConfiguration, String overrideExistingValues, String removeSourceValue) {
		this.facetConfiguration = facetConfiguration;
		this.overrideExistingValues = "true".equals(overrideExistingValues);
		this.removeSourceValue = "true".equals(removeSourceValue);
		
	}
	public FacetConfiguration getFacetConfiguration() {
		return facetConfiguration;
	}
	public void setFacetConfiguration(FacetConfiguration facetConfiguration) {
		this.facetConfiguration = facetConfiguration;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	

}
