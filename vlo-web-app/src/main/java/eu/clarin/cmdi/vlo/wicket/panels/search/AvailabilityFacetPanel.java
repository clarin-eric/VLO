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
import com.google.common.collect.Ordering;
import java.util.List;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.FixedSetFieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldsModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import java.io.Serializable;
import java.util.Comparator;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Dedicated panel for deselecting availability levels. Notice that this panel
 * allows for 'OR' selection on a number of preconfigured values
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AvailabilityFacetPanel extends ExpandablePanel<QueryFacetsSelection> {

    public static final String AVAILABILITY_FIELD = FacetConstants.FIELD_AVAILABILITY;
    public final static List<String> AVAILABILITY_LEVELS = ImmutableList.of("PUB", "ACA", "RES", FacetConstants.NO_VALUE);  //TODO - get these from config or global

    @SpringBean
    private FieldValueConverterProvider fieldValueConverterProvider;

    private final FacetFieldsModel facetFieldsModel;

    public AvailabilityFacetPanel(String id, final IModel<QueryFacetsSelection> selectionModel, FacetFieldsModel facetFieldsModel) {
        super(id, selectionModel);
        this.facetFieldsModel = facetFieldsModel;

        add(new Form("availability")
                .add(new DataView<Count>("option", getValuesProvider()) {
                    @Override
                    protected void populateItem(Item<Count> item) {
                        final String facetValue = item.getModelObject().getName();
                        item.add(createValueCheckbox("selector", facetValue));
                        item.add(new Label("label", new PropertyModel<String>(item.getModel(), "name")));
                    }

                })
                .add(createValueCheckbox("unk", FacetConstants.NO_VALUE))
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

