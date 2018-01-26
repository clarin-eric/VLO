package eu.clarin.cmdi.vlo.importer.mapping;

import eu.clarin.cmdi.vlo.importer.FacetConfiguration;

public class Target {
	private FacetConfiguration facetConfiguration;
	private String value;
	
	
	public Target(FacetConfiguration facetConfiguration, String value) {
		super();
		this.facetConfiguration = facetConfiguration;
		this.value = value;
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
