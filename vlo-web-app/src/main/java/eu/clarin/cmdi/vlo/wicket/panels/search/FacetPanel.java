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

import eu.clarin.cmdi.vlo.wicket.model.SolrFieldDescriptionModel;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetFieldSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import java.util.Collection;
import java.util.HashSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Panel that displays a single facet based on the current query/value
 * selection. Two children will be generated: a {@link FacetValuesPanel} and a
 * {@link SelectedFacetPanel}. One of them is set visible (at {@link
 * #onConfigure()}), depending on whether this facet has selected values.
 *
 * @author twagoo
 */
public abstract class FacetPanel extends ExpandablePanel<FacetFieldSelection> {

    private final IModel<ExpansionState> expansionStateModel;

    private final SelectedFacetPanel selectedFacetPanel;
    private final FacetValuesPanel facetValuesPanel;

    public FacetPanel(String id, IModel<FacetFieldSelection> selectionModel, IModel<ExpansionState> expansionState) {
        this(id, selectionModel, expansionState, 0);
    }

    public FacetPanel(String id, IModel<FacetFieldSelection> selectionModel, IModel<ExpansionState> expansionState, int subListSize) {
        super(id, selectionModel, expansionState);
        this.expansionStateModel = expansionState;

        // panel showing values for selection
        facetValuesPanel = createFacetValuesPanel("facetValues", subListSize);
        add(facetValuesPanel);

        // panel showing current selection, allowing for deselection
        selectedFacetPanel = createSelectedFacetPanel("facetSelection");
        add(selectedFacetPanel);
    }

    @Override
    protected Label createTitleLabel(String id) {
        final IModel<String> facetNameModel = new PropertyModel<>(getModel(), "facetField.name");
        final Label label = new Label(id, new SolrFieldNameModel(facetNameModel));
        label.add(new AttributeAppender("title", new SolrFieldDescriptionModel(facetNameModel)));
        return label;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final boolean valuesSelected = !getModelObject().getFacetValues().isEmpty();
        facetValuesPanel.setVisible(!valuesSelected);
        selectedFacetPanel.setVisible(valuesSelected);

        // hide this entire panel is no values are selectable
        setVisible(!isHideIfNoValues() || valuesSelected || getModelObject().getFacetField().getValueCount() > 0);
    }

    /**
     *
     * @return whether the panel should be hidden if no values are present given
     * the current selection and query
     */
    protected boolean isHideIfNoValues() {
        return true;
    }

    private FacetValuesPanel createFacetValuesPanel(String id, int subListSize) {
        return new FacetValuesPanel(id,
                new PropertyModel<FacetField>(getModel(), "facetField"),
                new PropertyModel<QueryFacetsSelection>(getModel(), "selection"), subListSize) {
                    @Override
                    public void onValuesSelected(String facet, FacetSelection value, AjaxRequestTarget target) {
                        // A value has been selected on this facet's panel, update the model!
                        FacetPanel.this.getModelObject().getSelection().selectValues(facet, value);
                        if (target != null) {
                            // reload entire page for now
                            selectionChanged(target);
                        }
                    }
                };
    }

    private SelectedFacetPanel createSelectedFacetPanel(String id) {
        return new SelectedFacetPanel(id, getModel()) {
            @Override
            public void onValuesUnselected(String facet, Collection<String> valuesRemoved, AjaxRequestTarget target) {
                final QueryFacetsSelection selection = getModelObject().getSelection();

                // Values have been removed, calculate remainder
                final FacetSelection facetSelection = selection.getSelectionValues(facet);
                final Collection<String> currentSelection = facetSelection.getValues();
                final Collection<String> newSelection = new HashSet<String>(currentSelection);
                newSelection.removeAll(valuesRemoved);

                // Update model (keep selection type)
                selection.selectValues(facet, new FacetSelection(facetSelection.getSelectionType(), newSelection));

                // collapse after removal
                // TODO: should be removed, but then list of values
                // does not seem to update correctly
                expansionStateModel.setObject(ExpansionState.COLLAPSED);

                if (target != null) {
                    // reload entire page for now
                    selectionChanged(target);
                }
            }
        };
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
