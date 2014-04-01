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

import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import java.util.Collection;
import java.util.HashSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
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
public abstract class FacetPanel extends Panel {

    private final IModel<FacetSelection> selectionModel;
    private final IModel<ExpansionState> expansionStateModel;

    private final SelectedFacetPanel selectedFacetPanel;
    private final FacetValuesPanel facetValuesPanel;

    public FacetPanel(String id, IModel<FacetSelection> selectionModel, IModel<ExpansionState> expansionState) {
        super(id, selectionModel);

        this.selectionModel = selectionModel;
        this.expansionStateModel = expansionState;

        // facet title annex expansion toggler
        add(createTitleToggler("titleToggle"));

        // panel showing values for selection
        facetValuesPanel = createFacetValuesPanel("facetValues");
        add(facetValuesPanel);

        // panel showing current selection, allowing for deselection
        selectedFacetPanel = createSelectedFacetPanel("facetSelection");
        add(selectedFacetPanel);

        addExpansionComponents();
    }

    private AjaxFallbackLink createTitleToggler(String id) {
        // facet title is also a link that toggles expansion state
        final AjaxFallbackLink titleLink = new IndicatingAjaxFallbackLink(id) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                final ExpansionState expansionState = expansionStateModel.getObject();
                if (expansionState == ExpansionState.COLLAPSED) {
                    expansionStateModel.setObject(ExpansionState.EXPANDED);
                } else {
                    expansionStateModel.setObject(ExpansionState.COLLAPSED);
                }
                if (target != null) {
                    target.add(FacetPanel.this);
                }
            }
        };

        // Facet name becomes title
        titleLink.add(new Label("title", new SolrFieldNameModel(new PropertyModel(selectionModel, "facetField.name"))));
        return titleLink;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final boolean valuesSelected = !selectionModel.getObject().getFacetValues().isEmpty();
        facetValuesPanel.setVisible(!valuesSelected);
        selectedFacetPanel.setVisible(valuesSelected);

        // hide this entire panel is no values are selectable
        setVisible(!isHideIfNoValues() || valuesSelected || selectionModel.getObject().getFacetField().getValueCount() > 0);
    }

    /**
     *
     * @return whether the panel should be hidden if no values are present given
     * the current selection and query
     */
    protected boolean isHideIfNoValues() {
        return true;
    }

    private FacetValuesPanel createFacetValuesPanel(String id) {
        return new FacetValuesPanel(id,
                new PropertyModel<FacetField>(selectionModel, "facetField"),
                new PropertyModel<QueryFacetsSelection>(selectionModel, "selection")) {
                    @Override
                    public void onValuesSelected(String facet, Collection<String> value, AjaxRequestTarget target) {
                        // A value has been selected on this facet's panel, update the model!
                        selectionModel.getObject().getSelection().selectValues(facet, value);
                        if (target != null) {
                            // reload entire page for now
                            selectionChanged(target);
                        }
                    }
                };
    }

    private SelectedFacetPanel createSelectedFacetPanel(String id) {
        return new SelectedFacetPanel(id, selectionModel) {
            @Override
            public void onValuesUnselected(String facet, Collection<String> valuesRemoved, AjaxRequestTarget target) {
                final QueryFacetsSelection selection = selectionModel.getObject().getSelection();

                // Values have been removed, calculate remainder
                final Collection<String> currentSelection = selection.getSelectionValues(facet);
                final Collection<String> newSelection = new HashSet<String>(currentSelection);
                newSelection.removeAll(valuesRemoved);

                // Update model
                selection.selectValues(facet, newSelection);

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

    private void addExpansionComponents() {

        // class modifier to apply correct class depending on state
        add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                switch (expansionStateModel.getObject()) {
                    case COLLAPSED:
                        return "facet collapsedfacet";
                    case EXPANDED:
                        return "facet expandedfacet";
                    default:
                        return "facet";
                }
            }
        }));

        // add expansion link
        add(new IndicatingAjaxFallbackLink("expand") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                expansionStateModel.setObject(ExpansionState.EXPANDED);
                if (target != null) {
                    target.add(FacetPanel.this);
                }
            }
        });

        // add collapse link
        add(new IndicatingAjaxFallbackLink("collapse") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                expansionStateModel.setObject(ExpansionState.COLLAPSED);
                if (target != null) {
                    target.add(FacetPanel.this);
                }
            }
        });
        setOutputMarkupId(true);
    }

    @Override
    public void detachModels() {
        // this will detach selection model (passed to super through constructor)
        super.detachModels();
        // additional model not known by supertype
        expansionStateModel.detach();
    }

    protected abstract void selectionChanged(AjaxRequestTarget target);

}
