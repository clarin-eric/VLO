package eu.clarin.cmdi.vlo.importer.mapping;

import eu.clarin.cmdi.vlo.importer.FacetConfiguration;

public class Target {
	private FacetConfiguration facetConfiguration;
	private boolean overwriteExistingValues;
	
	public FacetConfiguration getFacetConfiguration() {
		return facetConfiguration;
	}
	public void setFacetConfiguration(FacetConfiguration facetConfiguration) {
		this.facetConfiguration = facetConfiguration;
	}
	public boolean isOverwriteExistingValues() {
		return overwriteExistingValues;
	}
	public void setOverwriteExistingValues(boolean overwriteExistingValues) {
		this.overwriteExistingValues = overwriteExistingValues;
	}

}
