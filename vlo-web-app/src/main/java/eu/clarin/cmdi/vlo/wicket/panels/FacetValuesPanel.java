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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.AllFacetValuesPage;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import java.util.Collection;
import java.util.Collections;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 * A panel representing a single facet and its selectable values
 *
 * @author twagoo
 */
public abstract class FacetValuesPanel extends GenericPanel<FacetField> {

    private final static int maxNumberOfFacetsToShow = 10; //TODO: get from config

    private final ModalWindow valuesWindow;
    private final IModel<QueryFacetsSelection> selectionModel;

    public FacetValuesPanel(String id, final IModel<FacetField> model, final IModel<QueryFacetsSelection> selectionModel) {
        super(id, model);
        this.selectionModel = selectionModel;

        // provider that extracts values and counts from FacetField
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(model, maxNumberOfFacetsToShow);
        add(new DataView<Count>("facetValues", valuesProvider) {

            @Override
            protected void populateItem(final Item<Count> item) {
                addFacetValue(item);
            }
        });

        // create a popup window for all facet values
        valuesWindow = createAllValuesWindow("allValues");
        add(valuesWindow);
        
        // create a link for showing all values        
        add(createAllValuesLink("allFacetValuesLink"));
    }

    private void addFacetValue(final Item<Count> item) {
        item.setDefaultModel(new CompoundPropertyModel<Count>(item.getModel()));

        // link to select an individual facet value
        final Link selectLink = new IndicatingAjaxFallbackLink("facetSelect") {

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

    private ModalWindow createAllValuesWindow(String id) {
        final ModalWindow window = new ModalWindow(id) {

            @Override
            public IModel<String> getTitle() {
                return new SolrFieldNameModel(getModel(), "name");
            }

        };

        final AllFacetValuesPanel allValuesPanel = new AllFacetValuesPanel(window.getContentId(), getModel()) {

            @Override
            protected void onValuesSelected(String facet, Collection<String> values, AjaxRequestTarget target) {
                window.close(target);
                FacetValuesPanel.this.onValuesSelected(facet, values, target);
            }
        };
        window.addOrReplace(allValuesPanel);
        return window;
    }

    private Link createAllValuesLink(String id) {
        final Link link = new AjaxFallbackLink<FacetField>(id, getModel()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (target == null) {
                    // open a new page with values
                    setResponsePage(new AllFacetValuesPage(getModel(), selectionModel));
                } else {
                    // show values in a popup (requires JavaScript)
                    valuesWindow.show(target);
                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show if there actually are more values!
                setVisible(getModel().getObject().getValueCount() > maxNumberOfFacetsToShow);
            }

        };
        return link;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        selectionModel.detach();
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
