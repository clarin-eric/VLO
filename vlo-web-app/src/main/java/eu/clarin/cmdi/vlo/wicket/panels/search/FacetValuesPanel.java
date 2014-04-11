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

import com.google.common.collect.ImmutableSet;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.AllFacetValuesPage;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import java.util.Collection;
import java.util.Collections;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A panel representing a single facet and its selectable values
 *
 * @author twagoo
 */
public abstract class FacetValuesPanel extends GenericPanel<FacetField> {

    public final static int MAX_NUMBER_OF_FACETS_TO_SHOW = 10; //TODO: get from config
    public final static Collection<String> LOW_PRIORITY_VALUES = ImmutableSet.of("unknown", "unspecified", "");

    private final ModalWindow valuesWindow;
    private final FacetFieldValuesProvider valuesProvider;
    private final IModel<QueryFacetsSelection> selectionModel;
    private final WebMarkupContainer valuesContainer;
    private final IModel<String> filterModel;

    public FacetValuesPanel(String id, final IModel<FacetField> model, final IModel<QueryFacetsSelection> selectionModel) {
        super(id, model);
        this.selectionModel = selectionModel;

        // shared model that holds the string for filtering the values (quick search)
        filterModel = new Model<String>(null);
        // create a form with an input bound to the filter model
        add(createFilterForm("filter"));

        valuesProvider = new FacetFieldValuesProvider(model, MAX_NUMBER_OF_FACETS_TO_SHOW, LOW_PRIORITY_VALUES) {

            @Override
            protected IModel<String> getFilterModel() {
                return filterModel;
            }

        };

        // create a container for values to allow for AJAX updates when filtering
        valuesContainer = new WebMarkupContainer("valuesContainer");
        valuesContainer.setOutputMarkupId(true);
        add(valuesContainer);

        // create a view for the actual values
        final DataView<Count> valuesView = new DataView<Count>("facetValues", valuesProvider) {

            @Override
            protected void populateItem(final Item<Count> item) {
                addFacetValue(item);
            }
        };
        valuesView.setOutputMarkupId(true);
        valuesContainer.add(valuesView);

        // create a link for showing all values        
        valuesContainer.add(createAllValuesLink("allFacetValuesLink"));

        // create a popup window for all facet values
        valuesWindow = createAllValuesWindow("allValues");
        add(valuesWindow);
    }

    private Form createFilterForm(String id) {
        final Form filterForm = new Form(id);
        final TextField<String> filterField = new TextField<String>("filterText", filterModel);
        // make field update 
        filterField.add(new AjaxFormComponentUpdatingBehavior("keyup") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                //update values
                target.add(valuesContainer);
            }
        });
        filterForm.add(filterField);
        return filterForm;
    }

    private void addFacetValue(final Item<Count> item) {
        item.setDefaultModel(new CompoundPropertyModel<Count>(item.getModel()));

        // link to select an individual facet value
        final Link selectLink = new IndicatingAjaxFallbackLink("facetSelect") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                // reset filter
                filterModel.setObject(null);

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

        final AllFacetValuesPanel allValuesPanel = new AllFacetValuesPanel(window.getContentId(), getModel(), filterModel) {

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
                setVisible(getModel().getObject().getValueCount() > MAX_NUMBER_OF_FACETS_TO_SHOW);
            }

        };
        return link;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        selectionModel.detach();
        filterModel.detach();
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
