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

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.Collection;
import java.util.HashSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Panel that displays a single facet based on the current query/value
 * selection. Two children will be generated: a {@link FacetValuesPanel} and a
 * {@link SelectedFacetPanel}. One of them is set visible (at {@link
 * #onConfigure()}), depending on whether this facet has selected values.
 *
 * @author twagoo
 */
public abstract class FacetPanel extends Panel {

    private final IModel<FacetSelection> model;

    private final SelectedFacetPanel selectedFacetPanel;
    private final FacetValuesPanel facetValuesPanel;

    public FacetPanel(String id, IModel<FacetSelection> model) {
        super(id, model);
        this.model = model;

        // panel showing values for selection
        facetValuesPanel = createFacetValuesPanel("facetValues");
        add(facetValuesPanel);

        // panel showing current selection, allowing for deselection
        selectedFacetPanel = createSelectedFacetPanel("facetSelection");
        add(selectedFacetPanel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final boolean valuesSelected = !model.getObject().getFacetValues().isEmpty();
        facetValuesPanel.setVisible(!valuesSelected);
        selectedFacetPanel.setVisible(valuesSelected);
    }

    private FacetValuesPanel createFacetValuesPanel(String id) {
        return new FacetValuesPanel(id, new PropertyModel<FacetField>(model, "facetField")) {
            @Override
            public void onValuesSelected(String facet, Collection<String> value, AjaxRequestTarget target) {
                // A value has been selected on this facet's panel,
                // update the model!
                model.getObject().getSelection().selectValues(facet, value);
                if (target != null) {
                    // reload entire page for now
                    selectionChanged(target);
                }
            }
        };
    }

    private SelectedFacetPanel createSelectedFacetPanel(String id) {
        return new SelectedFacetPanel(id, model) {
            @Override
            public void onValuesUnselected(String facet, Collection<String> valuesRemoved, AjaxRequestTarget target) {
                final QueryFacetsSelection selection = model.getObject().getSelection();
                // Values have been removed, calculate remainder
                final Collection<String> currentSelection = selection.getSelectionValues(facet);
                final Collection<String> newSelection = new HashSet<String>(currentSelection);
                newSelection.removeAll(valuesRemoved);
                // Update model
                selection.selectValues(facet, newSelection);
                if (target != null) {
                    // reload entire page for now
                    selectionChanged(target);
                }
            }
        };
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
