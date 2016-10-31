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

import java.util.Collection;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SelectionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldDescriptionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Panel that displays a single facet based on the current query/value
 * selection. Two children will be generated: a {@link FacetValuesPanel} and a
 * {@link SelectedFacetPanel}. One of them is set visible (at {@link
 * #onConfigure()}), depending on whether this facet has selected values.
 *
 * @author twagoo
 */
public abstract class FacetPanel extends ExpandablePanel<String> {

    private final static Logger logger = LoggerFactory.getLogger(FacetPanel.class);

    private final SelectedFacetPanel selectedFacetPanel;
    private final FacetValuesPanel facetValuesPanel;

    public FacetPanel(String id, IModel<String> facetNameModel, IModel<FacetField> facetFieldModel, final IModel<QueryFacetsSelection> selectionModel, IModel<ExpansionState> expansionState) {
        this(id, facetNameModel, facetFieldModel, selectionModel, expansionState, 0);
    }

    public FacetPanel(String id, IModel<String> facetNameModel, IModel<FacetField> facetFieldModel, final IModel<QueryFacetsSelection> selectionModel, IModel<ExpansionState> expansionState, int subListSize) {
        super(id, facetNameModel, expansionState);

        // panel showing values for selection
        facetValuesPanel = createFacetValuesPanel("facetValues", facetNameModel.getObject(), facetFieldModel, selectionModel, subListSize);
        add(facetValuesPanel);

        // panel showing current selection, allowing for deselection
        selectedFacetPanel = createSelectedFacetPanel("facetSelection", facetNameModel.getObject(), selectionModel);
        add(selectedFacetPanel);

        add(new AttributeAppender("class", new AbstractReadOnlyModel() {
            @Override
            public Object getObject() {
                return (selectedFacetPanel.getModelObject().isEmpty()) ? "unselected" : "selected";
            }
        }, " "));
    }

    @Override
    protected Label createTitleLabel(String id) {
        final Label label = new Label(id, new SolrFieldNameModel(getModel()));
        label.add(new AttributeAppender("title", new SolrFieldDescriptionModel(getModel())));
        return label;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        
        final boolean valuesSelected = !selectedFacetPanel.getModelObject().isEmpty();
        //facetValuesPanel.setVisible(!valuesSelected);
        selectedFacetPanel.setVisible(valuesSelected);
        facetValuesPanel.setVisible(expansionModel.getObject() != ExpansionState.COLLAPSED);
        

        // hide this entire panel if nothing is selected or there is nothing to be selected
        //setVisible(!isHideIfNoValues() || valuesSelected || facetValuesPanel.getModelObject().getValueCount() > 0);
        
        setVisible(!isHideIfNoValues() || !selectedFacetPanel.getModelObject().isEmpty() || facetValuesPanel.getModelObject().getValueCount() > 0);
    }

    /**
     *
     * @return whether the panel should be hidden if no values are present given
     * the current selection and query
     */
    protected boolean isHideIfNoValues() {
        return true;
    }

    private FacetValuesPanel createFacetValuesPanel(String id, final String facetName, IModel<FacetField> facetFieldModel, final IModel<QueryFacetsSelection> selectionModel, int subListSize) {
        return new FacetValuesPanel(id, facetFieldModel, selectionModel, subListSize) {
            @Override
            public void onValuesSelected(Collection<String> values, AjaxRequestTarget target) {
                // A value has been selected on this facet's panel, update the model!
                selectionModel.getObject().addNewFacetValue(facetName, values);

                if (target != null) {
                    // reload entire page for now
                    selectionChanged(target);
                }
            }
        };
    }

    private SelectedFacetPanel createSelectedFacetPanel(String id, final String facetName, final IModel<QueryFacetsSelection> selectionModel) {
        return new SelectedFacetPanel(id, facetName, new SelectionModel(facetName, selectionModel)) {
            @Override
            public void onValuesUnselected(Collection<String> valuesRemoved, AjaxRequestTarget target) {
                // Values have been removed, calculate remainder
                selectionModel.getObject().removeFacetValue(facetName, valuesRemoved);

                // collapse after removal
                // TODO: should be removed, but then list of values
                // does not seem to update correctly
                expansionModel.setObject(ExpansionState.COLLAPSED);

                if (target != null) {
                    // reload entire page for now
                    selectionChanged(target);
                }
            }
        };
    }

    @Override
    protected void onExpandCollapse(AjaxRequestTarget target) {
        super.onExpandCollapse(target);
        if (target != null) {
            target.appendJavaScript("applyFacetTooltips();");
        }
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
