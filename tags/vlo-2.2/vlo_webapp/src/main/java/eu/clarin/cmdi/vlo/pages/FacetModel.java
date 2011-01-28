package eu.clarin.cmdi.vlo.pages;

import java.io.Serializable;

import org.apache.solr.client.solrj.response.FacetField;

public class FacetModel implements Serializable {

    private final FacetField facetField;
    private static final long serialVersionUID = 1L;
    private String selectedValue;

    public FacetModel(FacetField facetField) {
        this.facetField = facetField;
    }

    public FacetField getFacetField() {
        return facetField;
    }

    public boolean isSelected() {
        return selectedValue != null;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

}
