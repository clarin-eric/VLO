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

import eu.clarin.cmdi.vlo.wicket.components.FieldValueOrderSelector;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import java.util.Collection;
import java.util.Collections;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
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

/**
 * A panel that shows all available values for a selected facet. Supports two
 * ordering modes (by name or result count) and dynamic filtering.
 *
 * @author twagoo
 */
public abstract class AllFacetValuesPanel extends GenericPanel<FacetField> {

    private final FacetFieldValuesProvider valuesProvider;
    private final WebMarkupContainer valuesContainer;
    private IModel<String> filterModel = new Model<String>();

    public AllFacetValuesPanel(String id, IModel<FacetField> model) {
        super(id, model);
        // create a provider that shows all values and is sorted by name by default
        valuesProvider = new FacetFieldValuesProvider(model, Integer.MAX_VALUE, FieldValueOrderSelector.NAME_SORT) {

            @Override
            protected IModel<String> getFilterModel() {
                // filters the values
                return filterModel;
            }

        };

        // create a container for the values to allow for AJAX updates
        valuesContainer = new WebMarkupContainer("facetValuesContainer");
        valuesContainer.setOutputMarkupId(true);
        add(valuesContainer);

        // create the view of the actual values
        final DataView<FacetField.Count> valuesView = createValuesView("facetValue");
        valuesContainer.add(valuesView);

        // create the form for selection sort option and entering filter string
        final Form optionsForm = createOptionsForm("options");
        optionsForm.setOutputMarkupId(true);
        add(optionsForm);
    }

    private DataView<FacetField.Count> createValuesView(String id) {
        return new DataView<FacetField.Count>(id, valuesProvider) {

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
        };
    }

    private Form createOptionsForm(String id) {
        final Form options = new Form(id);
        final DropDownChoice<SortParam<FieldValuesOrder>> sortSelect
                = new FieldValueOrderSelector("sort", new PropertyModel<SortParam<FieldValuesOrder>>(valuesProvider, "sort"));
        sortSelect.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(options);
                target.add(valuesContainer);
            }
        });
        options.add(sortSelect);

        final TextField filterField = new TextField("filter", filterModel);
        filterField.add(new AjaxFormComponentUpdatingBehavior("keyup") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(valuesContainer);
            }
        });
        options.add(filterField);
        return options;
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
