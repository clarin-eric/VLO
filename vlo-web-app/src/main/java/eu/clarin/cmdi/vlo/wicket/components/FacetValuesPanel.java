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

import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import java.util.Collection;
import java.util.Collections;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 * A panel representing a single facet
 *
 * @author twagoo
 */
public abstract class FacetValuesPanel extends Panel {

    private final int maxNumberOfFacetsToShow = 10; //TODO: get from config

    public FacetValuesPanel(String id, IModel<FacetField> model) {
        super(id, model);
        setDefaultModel(new CompoundPropertyModel<FacetField>(model));

        // 'name' field from FacetField
        add(new Label("name"));

        // provider that extracts values and counts from FacetField
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(model, maxNumberOfFacetsToShow);
        add(new DataView<Count>("facetValues", valuesProvider) {

            @Override
            protected void populateItem(final Item<Count> item) {
                item.setDefaultModel(new CompoundPropertyModel<Count>(item.getModel()));
                final Link selectLink = new AjaxFallbackLink("facetSelect") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        FacetValuesPanel.this.onValuesSelected(
                                item.getModelObject().getFacetField().getName(),
                                Collections.singleton(item.getModelObject().getName()),
                                target);
                    }
                };
                item.add(selectLink);
                // 'name' field from Count (name of value)
                selectLink.add(new Label("name"));
                // 'count' field from Count (document count for value)
                selectLink.add(new Label("count"));
            }
        });
    }

    /**
     * Callback triggered when values have been selected on this facet
     *
     * @param facet name of the facet this panel represents
     * @param values selected values
     * @param target Ajax target allowing for a partial update. May be null
     * (fallback)!
     */
    public abstract void onValuesSelected(String facet, Collection<String> values, AjaxRequestTarget target);

}
