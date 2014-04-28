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
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * A panel representing a single facet and its selectable values
 *
 * @author twagoo
 */
public abstract class FacetValuesPanel extends GenericPanel<FacetField> {

    public final static int MAX_NUMBER_OF_FACETS_TO_SHOW = 10; //TODO: get from config
    public final static Collection<String> LOW_PRIORITY_VALUES = ImmutableSet.of("unknown", "unspecified", "");

    private final ModalWindow valuesWindow;
    private final IModel<QueryFacetsSelection> selectionModel;
    private final WebMarkupContainer valuesContainer;
    private final IModel<FieldValuesFilter> filterModel;

    /**
     * Creates a new panel with selectable values for a single facet
     *
     * @param id component id
     * @param model facet field model for this panel
     * @param selectionModel model holding the global query/facet selection
     */
    public FacetValuesPanel(String id, final IModel<FacetField> model, final IModel<QueryFacetsSelection> selectionModel) {
        super(id, model);
        this.selectionModel = selectionModel;

        // shared model that holds the string for filtering the values (quick search)
        filterModel = Model.of(new FieldValuesFilter());
        // create a form with an input bound to the filter model
        add(createFilterForm("filter"));

        // create a container for values to allow for AJAX updates when filtering
        valuesContainer = new WebMarkupContainer("valuesContainer");
        valuesContainer.setOutputMarkupId(true);
        add(valuesContainer);

        // create a view for the actual values
        valuesContainer.add(createValuesView("facetValues"));

        // create a link for showing all values        
        valuesContainer.add(createAllValuesLink("allFacetValuesLink"));

        // create a popup window for all facet values
        valuesWindow = createAllValuesWindow("allValues");
        add(valuesWindow);
    }

    /**
     * Creates a form with an input bound to the filter model
     *
     * @param id component id
     * @return filter form
     */
    private Form createFilterForm(String id) {
        final Form filterForm = new Form(id);
        final TextField<String> filterField = new TextField<String>("filterText",
                new PropertyModel<String>(filterModel, "name"));
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

    /**
     * Creates a view for the actual values (as links) for selection
     *
     * @param id component id
     * @return data view with value links
     */
    private DataView<Count> createValuesView(String id) {
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(getModel(), MAX_NUMBER_OF_FACETS_TO_SHOW, LOW_PRIORITY_VALUES) {

            @Override
            protected IModel<FieldValuesFilter> getFilterModel() {
                return filterModel;
            }

        };
        final DataView<Count> valuesView = new DataView<Count>(id, valuesProvider) {

            @Override
            protected void populateItem(final Item<Count> item) {
                addFacetValue("facetSelect", item);
            }
        };
        valuesView.setOutputMarkupId(true);
        return valuesView;
    }

    /**
     * Adds an individual facet value selection link to a dataview item
     *
     * @param item item to add link to
     */
    private void addFacetValue(String id, final Item<Count> item) {
        item.setDefaultModel(new CompoundPropertyModel<Count>(item.getModel()));

        // link to select an individual facet value
        final Link selectLink = new IndicatingAjaxFallbackLink(id) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                // reset filter
                filterModel.getObject().setName(null);

                // call callback
                onValuesSelected(
                        item.getModelObject().getFacetField().getName(),
                        // for now only single values can be selected
                        new FacetSelection(Collections.singleton(item.getModelObject().getName())),
                        target);
            }
        };
        item.add(selectLink);

        // 'name' field from Count (name of value)
        selectLink.add(new Label("name"));
        // 'count' field from Count (document count for value)
        selectLink.add(new Label("count"));
    }

    /**
     * Creates a link that leads to the 'all facet values' view, either as a
     * modal window (if JavaScript is enabled, see {@link #createAllValuesWindow(java.lang.String)
     * }) or by redirecting to {@link AllFacetValuesPage}
     *
     * @param id component id
     * @return 'show all values' link
     */
    private Link createAllValuesLink(String id) {
        final Link link = new IndicatingAjaxFallbackLink<FacetField>(id, getModel()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (target == null) {
                    // no JavaScript, open a new page with values
                    setResponsePage(new AllFacetValuesPage(getModel(), selectionModel));
                } else {
                    // JavaScript enabled, show values in a modal popup
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

    /**
     * Creates a modal window showing a {@link AllFacetValuesPanel} for this
     * facet
     *
     * @param id component id
     * @return 'all facet values' modal window component
     */
    private ModalWindow createAllValuesWindow(String id) {
        final ModalWindow window = new ModalWindow(id) {

            @Override
            public IModel<String> getTitle() {
                return new SolrFieldNameModel(getModel(), "name");
            }

        };
        window.showUnloadConfirmation(false);

        final AllFacetValuesPanel allValuesPanel = new AllFacetValuesPanel(window.getContentId(), getModel(), filterModel) {

            @Override
            protected void onValuesSelected(String facet, FacetSelection values, AjaxRequestTarget target) {
                window.close(target);
                FacetValuesPanel.this.onValuesSelected(facet, values, target);
            }
        };
        window.addOrReplace(allValuesPanel);
        return window;
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
    protected abstract void onValuesSelected(String facet, FacetSelection values, AjaxRequestTarget target);

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        // if an ajax update, set the watermark on the input field
        final AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
        if (target != null) {
            target.appendJavaScript(String.format("jQuery('#%1$s input').watermark('Type to search for more');", getMarkupId()));
            // focus? better only when expanded. jQuery('#%1$s input').focus()
        }
    }
}
