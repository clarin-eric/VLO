/*
 * Copyright (C) 2016 CLARIN
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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.List;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.FixedSetFieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.components.FieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.model.MapValueModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Dedicated panel for deselecting availability levels. Notice that this panel
 * allows for 'OR' selection on a number of preconfigured values. It also
 * assumes that every document has a value for this field.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public abstract class AvailabilityFacetPanel extends ExpandablePanel<QueryFacetsSelection> implements IAjaxIndicatorAware {

    private static final FieldKey AVAILABILITY_FIELD = FieldKey.LICENSE_TYPE;
    private final List<String> availabilityLevels;

    @SpringBean
    private FieldValueConverterProvider fieldValueConverterProvider;
    @SpringBean
    private VloConfig vloConfig;
    @SpringBean
    private PiwikConfig piwikConfig;
    @SpringBean
    private FieldNameService fieldNameService;

    private final FacetFieldsModel facetFieldsModel;

    private final AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender();

    public AvailabilityFacetPanel(String id, final IModel<QueryFacetsSelection> selectionModel, FacetFieldsModel facetFieldsModel) {
        super(id, selectionModel);
        this.facetFieldsModel = facetFieldsModel;
        this.availabilityLevels = ImmutableList.copyOf(getLevelsFromConfig(vloConfig));

        final AvailabilityValuesProvider valuesProvider = new AvailabilityValuesProvider();
        add(new Form("availability")
                .add(new AvailabilityDataView("option", valuesProvider))
                .add(indicatorAppender)
        );

        if (selectionModel.getObject().getSelectionValues(fieldNameService.getFieldName(AVAILABILITY_FIELD)) != null) {
            //if there any selection, make initially expanded
            getExpansionModel().setObject(ExpansionState.EXPANDED);
        }
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, "Availability");
    }

    @Override
    public void detachModels() {
        super.detachModels();
        facetFieldsModel.detach();
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return indicatorAppender.getMarkupId();
    }

    private static List<String> getLevelsFromConfig(VloConfig config) {
        return Lists.transform(config.getAvailabilityValues(), new Function<FieldValueDescriptor, String>() {
            @Override
            public String apply(FieldValueDescriptor input) {
                return input.getValue();
            }
        });
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

    private class AvailabilityDataView extends DataView<Count> {

        private final IModel<String> fieldNameModel = Model.of(fieldNameService.getFieldName(AVAILABILITY_FIELD));
        /**
         * Model of (a serializable copy of) the availability levels map with
         * descriptions and display names for each level
         */
        private final IModel<Map<String, FieldValueDescriptor>> descriptorsModel
                = new MapModel<>(ImmutableMap.copyOf(FieldValueDescriptor.toMap(vloConfig.getAvailabilityValues())));

        public AvailabilityDataView(String id, IDataProvider<Count> dataProvider) {
            super(id, dataProvider);
        }

        @Override
        protected void populateItem(Item<Count> item) {
            //model for actual value
            final PropertyModel<String> valueModel = new PropertyModel<>(item.getModel(), "name");

            //checkbox
            final Component selector = createValueCheckbox("selector", valueModel.getObject());
            item.add(selector);

            if (piwikConfig.isEnabled()) {
                selector.add(new AjaxPiwikTrackingBehavior.EventTrackingBehavior("click", PiwikEventConstants.PIWIK_EVENT_CATEGORY_FACET, PiwikEventConstants.PIWIK_EVENT_ACTION_AVAILABILITY));
            }

            //label
            item.add(new WebMarkupContainer("label")
                    //child label
                    .add(new FieldValueLabel("name", valueModel, fieldNameModel))
                    //count label
                    .add(new Label("count", new PropertyModel<>(item.getModel(), "count")))
                    //reference to checkbox
                    .add(new AttributeModifier("for", selector.getMarkupId()))
                    .add(new AttributeAppender("class", valueModel, " "))
            );

            //description as tooltip (title)
            final IModel<FieldValueDescriptor> descriptorModel = new MapValueModel<>(descriptorsModel, valueModel);
            final IModel<String> descriptionModel = new PropertyModel<>(descriptorModel, "description");
            item.add(new AttributeModifier("title", descriptionModel));
        }

        private Component createValueCheckbox(final String id, final String targetValue) {
            return new CheckBox(id, new FixedValueSetBooleanSelectionModel(fieldNameService.getFieldName(AVAILABILITY_FIELD), availabilityLevels, targetValue, getModel()))
                    .add(new OnChangeAjaxBehavior() {

                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            selectionChanged(target);
                        }

                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            super.updateAjaxAttributes(attributes);

                            attributes.getAjaxCallListeners().add(new AjaxCallListener()
                                    //disable checkboxes while updating via AJAX
                                    .onBeforeSend("$('form#availability input').prop('disabled', true);")
                                    //re-enable checkboxes afterwards
                                    .onDone("$('form#availability input').prop('disabled', false);"));
                        }

                    });

        }
    }

    /**
     * Provides availability values filtered by the set of configured values in
     * the specified order
     */
    private class AvailabilityValuesProvider extends FacetFieldValuesProvider {

        private final IModel<FieldValuesFilter> valuesFilter = new Model<FieldValuesFilter>(new FixedSetFieldValuesFilter(availabilityLevels));
        private final Ordering<Count> valuesOrdering = Ordering.from(new FacetNameComparator(availabilityLevels));

        public AvailabilityValuesProvider() {
            super(new FacetFieldModel(fieldNameService.getFieldName(AVAILABILITY_FIELD), facetFieldsModel), fieldValueConverterProvider);
        }

        @Override
        protected IModel<FieldValuesFilter> getFilterModel() {
            return valuesFilter;
        }

        @Override
        protected Ordering getOrdering() {
            return valuesOrdering;
        }
    }

    /**
     * Comparator on basis of the value of {@link Count#getName()}
     */
    private static class FacetNameComparator implements Comparator<Count>, Serializable {

        private final List<String> names;

        public FacetNameComparator(List<String> names) {
            this.names = ImmutableList.copyOf(names);
        }

        @Override
        public int compare(Count o1, Count o2) {
            return names.indexOf(o1.getName()) - names.indexOf(o2.getName());
        }

    }

}
