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

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * Model for FacetSelection that simply wraps a QueryFacetsSelection model and
 * 'filters' for the specified facet
 *
 * @author twagoo
 */
public class FacetSelectionModel extends AbstractReadOnlyModel<FacetSelection> implements FacetSelection {

    private final String facet;
    private final IModel<QueryFacetsSelection> selectionModel;

    /**
     * 
     * @param facet facet to represent selection for
     * @param selectionModel broad (multi-facet) selection model
     */
    public FacetSelectionModel(String facet, IModel<QueryFacetsSelection> selectionModel) {
        this.facet = facet;
        this.selectionModel = selectionModel;
    }

    @Override
    public String getFacet() {
        return facet;
    }

    @Override
    public List<String> getFacetValues() {
        return new CopyOnWriteArrayList<String>(getSelection().getSelectionValues(facet));
    }

    @Override
    public QueryFacetsSelection getSelection() {
        return selectionModel.getObject();
    }

    @Override
    public FacetSelection getObject() {
        return this;
    }

    @Override
    public void detach() {
        selectionModel.detach();
    }

}
