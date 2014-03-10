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
import org.apache.solr.client.solrj.response.FacetField.Count;
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
 * A panel representing a single facet and its selectable values
 *
 * @author twagoo
 */
public abstract class FacetValuesPanel extends GenericPanel<FacetField> {

    private final int maxNumberOfFacetsToShow = 10; //TODO: get from config

    private final WebMarkupContainer allValuesContainer;

    public FacetValuesPanel(String id, final IModel<FacetField> model) {
        super(id, model);

        // add title
        add(new SolrFieldNameLabel("title", new PropertyModel<String>(model, "name")));

        // provider that extracts values and counts from FacetField
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(model, maxNumberOfFacetsToShow);
        add(new DataView<Count>("facetValues", valuesProvider) {

            @Override
            protected void populateItem(final Item<Count> item) {
                addFacetValue(item);
            }
        });

        allValuesContainer = createAllValuesPanel("allValuesContainer");
        add(allValuesContainer);
        add(createAllValuesLink("allFacetValuesLink"));
    }

    private void addFacetValue(final Item<Count> item) {
        item.setDefaultModel(new CompoundPropertyModel<Count>(item.getModel()));

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
        selectLink.add(new Label("count"));
    }

    private WebMarkupContainer createAllValuesPanel(final String id) {
        final WebMarkupContainer container = new WebMarkupContainer(id);
        container.setOutputMarkupId(true);
        WebMarkupContainer allValuesPlaceholder = createPlaceHolder("allValues");
        container.add(allValuesPlaceholder);
        return container;
    }

    private AjaxFallbackLink createAllValuesLink(String id) {
        final AjaxFallbackLink link = new AjaxFallbackLink(id) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                final IModel<FacetField> model = FacetValuesPanel.this.getModel();
                final AllFacetValuesPanel allValuesPanel = new AllFacetValuesPanel("allValues", model) {

                    @Override
                    protected void onCanceled(AjaxRequestTarget target) {
                        hideAllValuesPanel();
                        if (target != null) {
                            target.add(allValuesContainer);
                        }
                    }

                    @Override
                    protected void onValuesSelected(String facet, Collection<String> values, AjaxRequestTarget target) {
                        hideAllValuesPanel();
                        onValuesSelected(facet, values, target);
                    }
                };
                allValuesContainer.addOrReplace(allValuesPanel);
                if (target != null) {
                    target.add(allValuesContainer);
                }
            }

            private void hideAllValuesPanel() {
                allValuesContainer.addOrReplace(createPlaceHolder("allValues"));
            }
        };
        return link;
    }

    private WebMarkupContainer createPlaceHolder(final String id) {
        final WebMarkupContainer placeholder = new WebMarkupContainer(id);
        placeholder.setVisible(false);
        return placeholder;
    }

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
