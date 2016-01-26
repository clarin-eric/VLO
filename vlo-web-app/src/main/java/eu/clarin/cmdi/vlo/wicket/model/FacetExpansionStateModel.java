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

import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import java.util.Map;
import org.apache.wicket.model.IModel;

/**
 * Expansion state model that looks up expansion state in a map with facet
 * name as key
 *
 * @author twagoo
 */
public class FacetExpansionStateModel implements IModel<ExpansionState> {

    private final IModel<String> facetName;
    private final IModel<Map<String, ExpansionState>> expansionStateMapModel;

    /**
     * 
     * @param facetModel model that holds the current facet
     * @param expansionStateMapModel model that holds the map of expansion states
     */
    public FacetExpansionStateModel(IModel<String> facetName, IModel<Map<String, ExpansionState>> expansionStateMapModel) {
        this.facetName = facetName;
        this.expansionStateMapModel = expansionStateMapModel;
    }

    @Override
    public ExpansionState getObject() {
        final String facet = facetName.getObject();
        final ExpansionState state = expansionStateMapModel.getObject().get(facet);
        if (state == null) {
            return ExpansionState.COLLAPSED;
        } else {
            return state;
        }
    }

    @Override
    public void setObject(ExpansionState object) {
        final String facet = facetName.getObject();
        expansionStateMapModel.getObject().put(facet, object);
    }

    @Override
    public void detach() {
    	facetName.detach();
        expansionStateMapModel.detach();
    }

}
