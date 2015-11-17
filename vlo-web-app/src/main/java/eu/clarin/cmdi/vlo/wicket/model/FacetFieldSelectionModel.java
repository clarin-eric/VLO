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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;

/**
 * Model for FacetFieldSelection that simply wraps a QueryFacetsSelection model
 * and 'filters' for the specified facet
 *
 * @author twagoo
 */
public class FacetFieldSelectionModel extends AbstractReadOnlyModel<FacetSelection> {

    private final IModel<QueryFacetsSelection> selectionModel;
    private final IModel<FacetField> facetFieldModel;

    /**
     *
     * @param facetFieldModel
     * @param selectionModel broad (multi-facet) selection model
     */
    public FacetFieldSelectionModel(IModel<FacetField> facetFieldModel, IModel<QueryFacetsSelection> selectionModel) {
        this.facetFieldModel = facetFieldModel;
        this.selectionModel = selectionModel;
    }

 
    //@Override
   	public IModel<FacetField> getFacetFieldAsModel() {
   		return facetFieldModel;
   	}
    
    
    //@Override
    public FacetField getFacetField() {
        return facetFieldModel.getObject();
    }
    


   // @Override
    public List<String> getFacetValues() {
        final FacetSelection selection = getObject();
        if (selection == null) {
            return Collections.emptyList();
        } else {
            final Collection<String> selectionValues = selection.getValues();
            if (selectionValues == null) {
                return Collections.emptyList();
            } else {
                return new CopyOnWriteArrayList<String>(selectionValues);
            }
        }
    }
    
   

	//@Override
	public IModel<QueryFacetsSelection> getSelectionAsModel() {
		return selectionModel;
	}
    

    //@Override
    public QueryFacetsSelection getSelection() {
        return selectionModel.getObject();
    }

    @Override
    public FacetSelection getObject() {
    	final String facetName = getFacetField().getName();
    	return getSelection().getSelectionValues(facetName);
    }

    @Override
    public void detach() {
        selectionModel.detach();
        facetFieldModel.detach();
    }

    @Override
    public String toString() {
        return String.format("[Field: %s; Selection: %s]", facetFieldModel.getObject().getName(), selectionModel.getObject().toString());
    }

	
}
