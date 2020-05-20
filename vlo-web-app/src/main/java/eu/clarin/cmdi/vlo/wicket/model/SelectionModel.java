package eu.clarin.cmdi.vlo.wicket.model;

import java.util.Collections;


import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;

/* Model for {@link SelectedFacetValues}
 * 
 */
public class SelectionModel implements IModel<FacetSelection> {

    private final IModel<QueryFacetsSelection> selectionModel;
    private final IModel<String> facetNameModel;

    public SelectionModel(IModel<String> facetNameModel, IModel<QueryFacetsSelection> selectionModel) {
        this.selectionModel = selectionModel;
        this.facetNameModel = facetNameModel;
    }

    @Override
    public FacetSelection getObject() {
        final FacetSelection facetSelection = selectionModel.getObject().getSelectionValues(facetNameModel.getObject());
        return facetSelection != null ? facetSelection : new FacetSelection(FacetSelectionType.AND, Collections.emptyList());
    }

    @Override
    public void detach() {
        selectionModel.detach();
    }

}
