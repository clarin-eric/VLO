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

import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldsDataProvider;
import java.util.Collection;
import java.util.HashSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel representing a group of facets.
 *
 * For each facet present (retrieved from the injected
 * {@link FacetFieldsService}, a panel is added. This is either a
 * {@link FacetValuesPanel}, allowing for selection of facet values, or a
 * {@link SelectedFacetPanel} representing a facet with selected values,
 * allowing for deselection of these values.
 *
 * @author twagoo
 */
public class FacetsPanel extends Panel {

    @SpringBean
    private FacetFieldsService facetFieldsService;
    private final IModel<QueryFacetsSelection> model;

    public FacetsPanel(final String id, IModel<QueryFacetsSelection> model) {
        super(id, model);
        this.model = model;

        add(new DataView<FacetField>("facets", new FacetFieldsDataProvider(facetFieldsService, model)) {

            @Override
            protected void populateItem(Item<FacetField> item) {
                createFacetPanel(item);
            }
        });
    }

    private void createFacetPanel(Item<FacetField> item) {
        // Is there a selection for this facet?
        final IModel<FacetField> facetFieldModel = item.getModel();
        final String facetName = facetFieldModel.getObject().getName();
        final Collection<String> selectionValues = model.getObject().getSelectionValues(facetName);

        // Show different panel, depending on selected values
        if (selectionValues == null || selectionValues.isEmpty()) {
            // No values selected, show value selection panel
            item.add(createFacetValuesPanel(facetFieldModel));
        } else {
            // Values selected, show selected values panel (with option to remove)
            item.add(createSelectedFacetPanel(facetName));
        }
    }

    private FacetValuesPanel createFacetValuesPanel(final IModel<FacetField> facetFieldModel) {
        return new FacetValuesPanel("facet", facetFieldModel) {

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

    private SelectedFacetPanel createSelectedFacetPanel(String facetName) {
        return new SelectedFacetPanel("facet", new FacetSelectionModel(facetName, model)) {

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
