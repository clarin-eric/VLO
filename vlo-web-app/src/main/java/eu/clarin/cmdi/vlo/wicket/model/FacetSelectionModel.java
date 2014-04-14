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
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class FacetSelectionModel implements IModel<FacetSelection> {

    private final IModel<QueryFacetsSelection> selection;
    private final String facet;

    public FacetSelectionModel(IModel<QueryFacetsSelection> selection, String facet) {
        this.selection = selection;
        this.facet = facet;
    }
    
    @Override
    public FacetSelection getObject() {
        return selection.getObject().getSelectionValues(facet);
    }

    @Override
    public void setObject(FacetSelection object) {
        selection.getObject().selectValues(facet, object);
    }

    @Override
    public void detach() {
        selection.detach();
    }

}
