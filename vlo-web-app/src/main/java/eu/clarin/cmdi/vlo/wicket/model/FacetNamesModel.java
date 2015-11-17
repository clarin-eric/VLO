package eu.clarin.cmdi.vlo.wicket.model;

import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;

public class FacetNamesModel extends AbstractReadOnlyModel<List<String>>{
	
    private final List<String> facets;

    
    public FacetNamesModel(List<String> facets) {
        this.facets = facets;
    }

	@Override
	public List<String> getObject() {
		return facets;
	}
	
	

}
