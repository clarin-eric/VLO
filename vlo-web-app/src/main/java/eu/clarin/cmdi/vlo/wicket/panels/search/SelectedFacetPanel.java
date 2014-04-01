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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * A panel representing a single facet and its selected values, allowing for
 * deselection
 *
 * @author twagoo
 */
public abstract class SelectedFacetPanel extends Panel {

    private final IModel<FacetSelection> model;

    public SelectedFacetPanel(String id, final IModel<FacetSelection> model) {
        super(id, model);
        this.model = model;

        // Add removers for all selected values for collapsed state
        add(createSelectionRemovers("facetValueRemover"));
        // Add selected items to expanded state
        add(createSelectionRemovers("selectedItem"));
    }

    private ListView<String> createSelectionRemovers(String id) {
        // Model of the list of selected values in this facet
        final PropertyModel<List<String>> propertyModel = new PropertyModel<List<String>>(model, "facetValues");
        // Repeating container of value + unselection links
        final ListView<String> listView = new ListView<String>(id, propertyModel) {

            /**
             * Populates an individual value selection remover
             *
             * @param item item to populate
             */
            @Override
            protected void populateItem(final ListItem<String> item) {
                // A label showing the name of the facet
                item.add(new Label("facetValue", item.getModel()));
                // A link to remove the value selection from this facet
                item.add(new RemoveLink("unselectValue", item.getModel()));
            }

        };
        return listView;
    }

    /**
     * Callback triggered when values have been removed from this facet
     *
     * @param facet name of the facet this panel represents
     * @param valuesRemoved removed values
     * @param target Ajax target allowing for a partial update. May be null
     * (fallback)!
     */
    protected abstract void onValuesUnselected(String facet, Collection<String> valuesRemoved, AjaxRequestTarget target);

    public class RemoveLink extends IndicatingAjaxFallbackLink {

        private final IModel<String> valueModel;

        public RemoveLink(String id, IModel<String> valueModel) {
            super(id);
            this.valueModel = valueModel;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            // Remove a single value
            // Call callback
            onValuesUnselected(
                    model.getObject().getFacetField().getName(),
                    Collections.singleton(valueModel.getObject()), target);
        }

    }
}
