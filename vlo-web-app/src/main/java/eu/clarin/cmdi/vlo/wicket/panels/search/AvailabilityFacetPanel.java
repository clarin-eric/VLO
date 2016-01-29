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

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionValueQualifier;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import eu.clarin.cmdi.vlo.wicket.model.FacetFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated panel for deselecting availability levels. Notice that this panel
 * allows for 'OR' selection on a number of preconfigured values
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AvailabilityFacetPanel extends ExpandablePanel<QueryFacetsSelection> {

    private final static Logger log = LoggerFactory.getLogger(AvailabilityFacetPanel.class);
    public static final String AVAILABILITY_FIELD = FacetConstants.FIELD_AVAILABILITY;
    public final static List<String> availableValues = Arrays.asList("PUB", "ACA", "RES", FacetConstants.NO_VALUE);  //TODO - get these from config or global

    @SpringBean
    private FacetFieldsService facetFieldsService;
    @SpringBean
    private FieldValueConverterProvider fieldValueConverterProvider;

    public AvailabilityFacetPanel(String id, final IModel<QueryFacetsSelection> selectionModel) {
        super(id, selectionModel);
        final FacetSelectionModel fieldSelectionModel = new FacetSelectionModel(selectionModel, AVAILABILITY_FIELD);

        final IModel<FacetField> facetFieldModel = new FacetFieldModel(AVAILABILITY_FIELD, facetFieldsService, selectionModel);
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(facetFieldModel, Collections.<String>emptySet(), fieldValueConverterProvider) {
            @Override
            protected IModel<FieldValuesFilter> getFilterModel() {
                return super.getFilterModel(); //TODO: filter out all except for fixed items
            }

            @Override
            protected Ordering getOrdering() {
                return super.getOrdering(); //TODO: return fixed order
            }

        };

        add(new Form("availability")
                .add(new DataView<Count>("option", valuesProvider) {
                    @Override
                    protected void populateItem(Item<Count> item) {
                        final String facetValue = item.getModelObject().getName();
                        item.add(createValueCheckbox("selector", fieldSelectionModel, selectionModel, facetValue));
                        item.add(new Label("label", new PropertyModel<String>(item.getModel(), "name")));
                    }

                })
                .add(createValueCheckbox("unk", fieldSelectionModel, selectionModel, FacetConstants.NO_VALUE))
        );
    }

    private Component createValueCheckbox(final String id, final FacetSelectionModel fieldSelectionModel, final IModel<QueryFacetsSelection> selectionModel, final String targetValue) {
        return new CheckBox(id, new AvailabilityLevelBooleanModel(targetValue, fieldSelectionModel, selectionModel))
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

    private static class AvailabilityLevelBooleanModel implements IModel<Boolean> {

        private final FacetSelectionModel fieldSelectionModel;
        private final String targetValue;
        private final IModel<QueryFacetsSelection> selectionModel;

        public AvailabilityLevelBooleanModel(String targetValue, FacetSelectionModel fieldSelectionModel, IModel<QueryFacetsSelection> selectionModel) {
            this.fieldSelectionModel = fieldSelectionModel;
            this.targetValue = targetValue;
            this.selectionModel = selectionModel;
        }

        @Override
        public Boolean getObject() {
            final FacetSelection selection = fieldSelectionModel.getObject();
            if (selection == null) {
                //no selection -> no deselection
                return true;
            }

            final Collection<String> selectedValues = selection.getValues();
            final boolean valueExcluded = selection.getQualifier(targetValue) == FacetSelectionValueQualifier.NOT;
            switch (selection.getSelectionType()) {
                case AND:
                    return !(valueExcluded && selectedValues.contains(targetValue));
                case OR:
                    return !valueExcluded && selectedValues.contains(targetValue);
                default:
                    log.warn("Selection type neither AND or OR!");
                    return true;
            }
        }

        @Override
        public void setObject(Boolean select) {
            // try to keep existing selection if present, but only if type is OR
            FacetSelection selection = fieldSelectionModel.getObject();
            if (selection == null || selection.getSelectionType() != FacetSelectionType.OR) {
                if (select) {
                    //select, so none used to be selected
                    selection = new FacetSelection(FacetSelectionType.OR);
                } else {
                    //unselect, so all used to be selected
                    selection = new FacetSelection(FacetSelectionType.OR, availableValues);
                }
            }

            if (select) {
                selection.addValue(targetValue, null);
            } else {
                selection.removeValues(Collections.singleton(targetValue));
            }

            if (selection.getValues().isEmpty()) {
                //make a negative selection on all available values
                final FacetSelection negativeSelection = new FacetSelection(FacetSelectionType.AND);
                for (String val : availableValues) {
                    negativeSelection.addValue(val, FacetSelectionValueQualifier.NOT);
                }
                selectionModel.getObject().selectValues(AVAILABILITY_FIELD, negativeSelection);
            } else {
                // set on selection model (needed in case it is a new facet selection object)
                selectionModel.getObject().selectValues(AVAILABILITY_FIELD, selection);
            }
        }

        @Override
        public void detach() {
            fieldSelectionModel.detach();
        }
    }

}
