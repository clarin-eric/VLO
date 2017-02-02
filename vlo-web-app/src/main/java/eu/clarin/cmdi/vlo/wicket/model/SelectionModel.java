package eu.clarin.cmdi.vlo.wicket.model;

import java.util.Collections;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;

/* Model for {@link SelectedFacetValues}
 * 
 */
public class SelectionModel extends AbstractReadOnlyModel<FacetSelection> {

    private final IModel<QueryFacetsSelection> selectionModel;
    private final String facetName;

    public SelectionModel(String facetName, IModel<QueryFacetsSelection> selectionModel) {
        this.selectionModel = selectionModel;
        this.facetName = facetName;
    }

    @Override
    public FacetSelection getObject() {
        final FacetSelection facetSelection = selectionModel.getObject().getSelectionValues(facetName);
        return facetSelection != null ? facetSelection : new FacetSelection(FacetSelectionType.AND, Collections.EMPTY_LIST);
    }

    @Override
    public void detach() {
        super.detach();
        selectionModel.detach();
    }

}
