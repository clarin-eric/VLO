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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import java.util.Collection;
import java.util.HashSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public abstract class AbstractFacetsPanel extends Panel {

    protected final IModel<QueryFacetsSelection> model;

    public AbstractFacetsPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
        this.model = model;
    }

    protected FacetValuesPanel createFacetValuesPanel(String id, final IModel<FacetField> facetFieldModel) {
        return new FacetValuesPanel(id, facetFieldModel) {
            @Override
            public void onValuesSelected(String facet, Collection<String> value, AjaxRequestTarget target) {
                // A value has been selected on this facet's panel,
                // update the model!
                model.getObject().selectValues(facet, value);
                if (target != null) {
                    // reload entire page for now
                    target.add(getPage());
                }
            }
        };
    }

    protected SelectedFacetPanel createSelectedFacetPanel(String id, String facetName) {
        return new SelectedFacetPanel(id, new FacetSelectionModel(facetName, model)) {
            @Override
            public void onValuesUnselected(String facet, Collection<String> valuesRemoved, AjaxRequestTarget target) {
                // Values have been removed, calculate remainder
                final Collection<String> currentSelection = model.getObject().getSelectionValues(facet);
                final Collection<String> newSelection = new HashSet<String>(currentSelection);
                newSelection.removeAll(valuesRemoved);
                // Update model
                model.getObject().selectValues(facet, newSelection);
                if (target != null) {
                    // reload entire page for now
                    target.add(getPage());
                }
            }
        };
    }

}
