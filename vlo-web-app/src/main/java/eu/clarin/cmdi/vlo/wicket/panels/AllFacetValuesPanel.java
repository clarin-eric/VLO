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

import eu.clarin.cmdi.vlo.wicket.components.SolrFieldNameLabel;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import java.util.Collection;
import java.util.Collections;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author twagoo
 */
public abstract class AllFacetValuesPanel extends GenericPanel<FacetField> {

    public AllFacetValuesPanel(String id, IModel<FacetField> model) {
        super(id, model);

        add(new AjaxFallbackLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                onCanceled(target);
            }
        });

        // add title
        add(new SolrFieldNameLabel("title", new PropertyModel<String>(model, "name")));

        // provider that extracts values and counts from FacetField
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(model);
        add(new DataView<FacetField.Count>("facetValue", valuesProvider) {

            @Override
            protected void populateItem(final Item<FacetField.Count> item) {
                item.setDefaultModel(new CompoundPropertyModel<FacetField.Count>(item.getModel()));

                // link to select an individual facet value
                final Link selectLink = new AjaxFallbackLink("facetSelect") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        // call callback
                        onValuesSelected(
                                item.getModelObject().getFacetField().getName(),
                                // for now only single values can be selected
                                Collections.singleton(item.getModelObject().getName()),
                                target);
                    }
                };
                item.add(selectLink);

                // 'name' field from Count (name of value)
                selectLink.add(new Label("name"));

                // 'count' field from Count (document count for value)
                item.add(new Label("count"));
            }
        });
    }

    protected abstract void onCanceled(AjaxRequestTarget target);

    /**
     * Callback triggered when values have been selected on this facet
     *
     * @param facet name of the facet this panel represents
     * @param values selected values
     * @param target Ajax target allowing for a partial update. May be null
     * (fallback)!
     */
    protected abstract void onValuesSelected(String facet, Collection<String> values, AjaxRequestTarget target);

}
