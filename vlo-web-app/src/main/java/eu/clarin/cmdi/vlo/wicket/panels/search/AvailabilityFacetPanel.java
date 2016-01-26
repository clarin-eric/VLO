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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import java.util.HashSet;
import java.util.Set;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated panel for deselecting availability levels. Notice that this panel
 * works on basis of negative selection, i.e. boxes are checked UNLESS the
 * corresponding value is explicitly excluded in a NOT query.
 *
 * @see FacetSelectionType#NOT
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AvailabilityFacetPanel extends ExpandablePanel<QueryFacetsSelection> {

    private final static Logger log = LoggerFactory.getLogger(AvailabilityFacetPanel.class);

    public AvailabilityFacetPanel(String id, final IModel<QueryFacetsSelection> selectionModel) {
        super(id, selectionModel);
        final FacetSelectionModel fieldSelectionModel = new FacetSelectionModel(selectionModel, FacetConstants.FIELD_AVAILABILITY);

        add(new Form("availability")
                .add(createValueCheckbox("pub", fieldSelectionModel, selectionModel, FacetConstants.AVAILABILITY_LEVEL_PUB))
                .add(createValueCheckbox("aca", fieldSelectionModel, selectionModel, FacetConstants.AVAILABILITY_LEVEL_ACA))
                .add(createValueCheckbox("res", fieldSelectionModel, selectionModel, FacetConstants.AVAILABILITY_LEVEL_RES))
                .add(createValueCheckbox("unk", fieldSelectionModel, selectionModel, FacetConstants.AVAILABILITY_LEVEL_UNKNOWN))
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
            
            if (selection.getSelectionType() == FacetSelectionType.AND) {
                return selection.getValues().contains(targetValue);
            } else if (selection.getSelectionType() == FacetSelectionType.NOT) {
                return !selection.getValues().contains(targetValue);
            } else {
                return true;
            }
        }

        @Override
        public void setObject(Boolean select) {
            // try to keep existing selection if present, but make sure it has 
            // selection type NOT
            FacetSelection selection = fieldSelectionModel.getObject();
            if (selection == null || selection.getSelectionType() != FacetSelectionType.NOT) {
                selection = new FacetSelection(FacetSelectionType.NOT);
            }

            final Set<String> values = new HashSet<>(selection.getValues());
            if (select) {
                //allow this value, i.e. remove from NOT
                values.remove(targetValue);
            } else {
                //disallow this value, i.e. include in NOT
                values.add(targetValue);
            }
            selection.setValues(values);

            selectionModel.getObject().selectValues(FacetConstants.FIELD_AVAILABILITY, selection);
        }

        @Override
        public void detach() {
            fieldSelectionModel.detach();
        }
    }

}
