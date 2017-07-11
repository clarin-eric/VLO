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

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionValueQualifier;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.FacetSelectionModel;
import java.util.Collection;
import java.util.Collections;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To be used on checkboxes in facet selectors that have a fixed set of values
 * available for selection
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FixedValueSetBooleanSelectionModel implements IModel<Boolean> {

    private final static Logger logger = LoggerFactory.getLogger(AvailabilityFacetPanel.class);
    private final IModel<FacetSelection> fieldSelectionModel;
    private final IModel<QueryFacetsSelection> selectionModel;
    private final String facet;
    private final String targetValue;
    private final Collection<String> allowedValues;

    public FixedValueSetBooleanSelectionModel(String facet, Collection<String> allowedValues, String targetValue, IModel<QueryFacetsSelection> selectionModel) {
        this.facet = facet;
        this.allowedValues = allowedValues;
        this.targetValue = targetValue;
        this.fieldSelectionModel = new FacetSelectionModel(selectionModel, facet);
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
                logger.warn("Selection type neither AND or OR!");
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
                selection = new FacetSelection(FacetSelectionType.OR, allowedValues);
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
            for (String val : allowedValues) {
                negativeSelection.addValue(val, FacetSelectionValueQualifier.NOT);
            }
            selectionModel.getObject().selectValues(facet, negativeSelection);
        } else {
            // set on selection model (needed in case it is a new facet selection object)
            selectionModel.getObject().selectValues(facet, selection);
        }
    }

    @Override
    public void detach() {
        fieldSelectionModel.detach();
        selectionModel.detach();
    }

}
