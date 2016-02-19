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
package eu.clarin.cmdi.vlo.wicket.panels;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.panels.search.FacetPanel;

/**
 *
 * @author twagoo
 */
public abstract class SingleFacetPanel extends FacetPanel {
	
	//public FacetPanel(String id, IModel<String> facetName, IModel<FacetFieldSelection> selectionModel, IModel<ExpansionState> expansionState, int subListSize) {

    public SingleFacetPanel(String id, String facetName, IModel<QueryFacetsSelection> queryModel,  FacetFieldsService facetFieldsService, int subListSize) {
        //TODO: Limit to number of items shown while keeping 'more' function?
    	super(id, 
    			new Model(facetName), 
    			new FacetFieldModel(facetName, facetFieldsService, queryModel), 
    			queryModel, 
				Model.of(ExpansionState.COLLAPSED),
				subListSize);
    }

    @Override
    protected boolean isHideIfNoValues() {
        // collections facets should always be visible, even if no
        // values are present (due to no search results)
        return false;
    }

}
