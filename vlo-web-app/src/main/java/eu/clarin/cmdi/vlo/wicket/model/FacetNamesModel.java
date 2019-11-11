package eu.clarin.cmdi.vlo.wicket.model;

import java.util.List;

import org.apache.wicket.model.IModel;

public class FacetNamesModel implements IModel<List<String>> {

    private final List<String> facets;

    public FacetNamesModel(List<String> facets) {
        this.facets = facets;
    }

    @Override
    public List<String> getObject() {
        return facets;
    }

}
