/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.wicket.model;

import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionValueQualifier;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;

/**
 * Model that provides a list of {@link FacetField}s based on the current query
 * and values selection, filtered through a selection of facet names.
 *
 * Notice that the actual retrieval is carried out by the provided
 * {@link FacetFieldsService}, which therefore should be configured to actually
 * retrieve the specified facets (in the list in the constructor), otherwise
 * some of these may not be present.
 *
 * @author twagoo
 */
public class FacetFieldsModel extends LoadableDetachableModel<List<FacetField>> {

    private final FacetFieldsService service;
    private final List<String> facets;
    private final IModel<QueryFacetsSelection> selectionModel;
    private final int valueLimit;

    /**
     * now we are returning all facets and model is shared between
     * facetValuesPanel and FacetsPagePanel
     *
     * @param service service to use for facet field retrieval
     * @param facets facets to include
     * @param selectionModel model that provides current query/selection
     */
    public FacetFieldsModel(FacetFieldsService service, List<String> facets, IModel<QueryFacetsSelection> selectionModel) {
        this(service, facets, selectionModel, -1);
    }

    /**
     *
     * @param service service to use for facet field retrieval
     * @param facets facets to include
     * @param selectionModel model that provides current query/selection
     * @param valueLimit maximum number of values to retrieve per facet.
     * Negative for unlimited
     */
    public FacetFieldsModel(FacetFieldsService service, List<String> facets, IModel<QueryFacetsSelection> selectionModel, int valueLimit) {
        this.service = service;
        this.facets = facets;
        this.selectionModel = selectionModel;
        this.valueLimit = valueLimit;
    }

    @Override
    protected List<FacetField> load() {
        return service.getFacetFields(selectionModel.getObject(), facets, valueLimit);
    }

    @Override
    public void detach() {
        super.detach();
        selectionModel.detach();
    }

    public FacetField getFacetField(String facetName) {
        List<FacetField> facetList = getObject();
        if (facetList != null) {
            for (FacetField facet : facetList) {
                if (facet.getName().equals(facetName)) {
                    return removeSelected(facet, selectionModel.getObject().getSelectionValues(facetName));
                }
            }
        }

        return null;
    }

    private FacetField removeSelected(FacetField facetField, FacetSelection selection) {
        FacetField filtered = new FacetField(facetField.getName());
        if (selection != null
                && selection.getSelectionType() == FacetSelectionType.AND) {
            //we want to exclude the selected option(s) in case of an AND selection
            for (FacetField.Count value : facetField.getValues()) {
                final String valueName = value.getName();
                if (selection.getValues().contains(valueName)
                        //exclude negative value selectors
                        //(not such a great solution, added to make availability facet checkboxes work)
                        && selection.getQualifier(valueName) != FacetSelectionValueQualifier.NOT
                        ) {
                    continue;
                } else {
                    filtered.add(valueName, value.getCount());
                }
            }
            return filtered;
        } else {
            return facetField;
        }

    }

}
