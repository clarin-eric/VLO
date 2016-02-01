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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import java.util.List;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.FixedSetFieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
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
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
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
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AvailabilityFacetPanel extends ExpandablePanel<QueryFacetsSelection> {

    public static final String AVAILABILITY_FIELD = FacetConstants.FIELD_AVAILABILITY;
    public final static List<String> AVAILABILITY_LEVELS = ImmutableList.of("PUB", "ACA", "RES", "UNSPECIFIED");  //TODO - get these from config or global + description

    @SpringBean
    private FieldValueConverterProvider fieldValueConverterProvider;
    @SpringBean
    private VloConfig vloConfig;

    private final FacetFieldsModel facetFieldsModel;

    public AvailabilityFacetPanel(String id, final IModel<QueryFacetsSelection> selectionModel, FacetFieldsModel facetFieldsModel) {
        super(id, selectionModel);
        this.facetFieldsModel = facetFieldsModel;

        //some models that we can reuse:
        final IModel<String> fieldNameModel = Model.of(AVAILABILITY_FIELD);
        //model of (a serializable copy of) the availability levels map with descriptions and display names for each level
        final IModel<Map<String, FieldValueDescriptor>> descriptorsModel
                = new MapModel<>(Maps.newHashMap(FieldValueDescriptor.toMap(vloConfig.getAvailabilityValues())));

        add(new Form("availability")
                .add(new DataView<Count>("option", getValuesProvider()) {
                    @Override
                    protected void populateItem(Item<Count> item) {
                        //model for actual value
                        final PropertyModel<String> valueModel = new PropertyModel<>(item.getModel(), "name");

                        //checkbox
                        final Component selector = createValueCheckbox("selector", valueModel.getObject());
                        item.add(selector);

                        //label 
                        item.add(new WebMarkupContainer("label")
                                //child label
                                .add(new FieldValueLabel("name", valueModel, fieldNameModel))
                                //count label
                                .add(new Label("count", new PropertyModel<String>(item.getModel(), "count")))
                                //reference to checkbox
                                .add(new AttributeModifier("for", selector.getMarkupId()))
                        );

                        //description as tooltip (title)
                        final IModel<FieldValueDescriptor> descriptorModel = new MapValueModel<>(descriptorsModel, valueModel);
                        final IModel<String> descriptionModel = new PropertyModel<>(descriptorModel, "description");
                        item.add(new AttributeModifier("title", descriptionModel));
                    }
                })
        );
    }

    private FacetFieldValuesProvider getValuesProvider() {
        final IModel<FacetField> facetFieldModel = new FacetFieldModel(AVAILABILITY_FIELD, facetFieldsModel);
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(facetFieldModel, fieldValueConverterProvider) {
            final IModel<FieldValuesFilter> valuesFilter = new Model<FieldValuesFilter>(new FixedSetFieldValuesFilter(AVAILABILITY_LEVELS));
            final Ordering<Count> valuesOrdering = Ordering.from(new FacetNameComparator(AVAILABILITY_LEVELS));

            @Override
            protected IModel<FieldValuesFilter> getFilterModel() {
                return valuesFilter;
            }

            @Override
            protected Ordering getOrdering() {
                return valuesOrdering;
            }
        };
        return valuesProvider;
    }

    private Component createValueCheckbox(final String id, final String targetValue) {
        return new CheckBox(id, new FixedValueSetBooleanSelectionModel(AVAILABILITY_FIELD, AVAILABILITY_LEVELS, targetValue, getModel()))
                .add(new OnChangeAjaxBehavior() {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        selectionChanged(target);
                    }
                });
    }

    @Override
    protected Label createTitleLabel(String id) {
        return new Label(id, "Availability");
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

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

    @Override
    public void detachModels() {
        super.detachModels();
        facetFieldsModel.detach();;
    }

}
