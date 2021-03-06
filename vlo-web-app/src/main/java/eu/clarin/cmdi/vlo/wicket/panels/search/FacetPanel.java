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

import eu.clarin.cmdi.vlo.wicket.components.AjaxIndicatingTextField;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import java.util.Collection;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.model.SelectionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldDescriptionModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.panels.ExpandablePanel;
import java.util.Optional;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;

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
    private final static Duration FILTER_RESPONSE_THROTTLE = Duration.milliseconds(250L);

    @SpringBean
    private PiwikConfig piwikConfig;

    private final SelectedFacetPanel selectedFacetPanel;
    private final FacetValuesPanel facetValuesPanel;
    private final IModel<FacetSelectionType> selectionTypeModeModel;
    private final IModel<FieldValuesFilter> filterModel;

    public FacetPanel(String id, IModel<String> facetNameModel, IModel<FacetField> facetFieldModel, final IModel<QueryFacetsSelection> selectionModel, final IModel<FacetSelectionType> selectionTypeModeModel, IModel<ExpansionState> expansionState, IModel<FieldValuesFilter> filterModel) {
        this(id, facetNameModel, facetFieldModel, selectionModel, selectionTypeModeModel, expansionState, filterModel, 0);
    }

    public FacetPanel(String id, IModel<String> facetNameModel, IModel<FacetField> facetFieldModel, final IModel<QueryFacetsSelection> selectionModel, final IModel<FacetSelectionType> selectionTypeModeModel, IModel<ExpansionState> expansionState, IModel<FieldValuesFilter> filterModel, int subListSize) {
        super(id, facetNameModel, expansionState);
        this.selectionTypeModeModel = selectionTypeModeModel;

        // shared model that holds the string for filtering the values (quick search)
        this.filterModel = filterModel;
        // create a form with an input bound to the filter model
        add(createFilterForm("filter"));

        // panel showing values for selection
        facetValuesPanel = createFacetValuesPanel("facetValues", facetNameModel, facetFieldModel, selectionModel, subListSize);
        add(facetValuesPanel);

        // panel showing current selection, allowing for deselection
        selectedFacetPanel = createSelectedFacetPanel("facetSelection", facetNameModel, selectionModel);
        add(selectedFacetPanel);

        add(new AttributeAppender("class", new IModel() {
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
    protected Link createTitleToggler() {
        final Link link = super.createTitleToggler();
        if (piwikConfig.isEnabled()) {
            link.add(new AjaxPiwikTrackingBehavior.EventTrackingBehavior("click", PiwikEventConstants.PIWIK_EVENT_CATEGORY_FACET, PiwikEventConstants.PIWIK_EVENT_ACTION_FACET_EXPANDCOLLAPSE) {
                @Override
                protected String getName(AjaxRequestTarget target) {
                    return FacetPanel.this.getModelObject();
                }

                @Override
                protected String getValue(AjaxRequestTarget target) {
                    return getExpansionModel().getObject().toString().toLowerCase();
                }

            });
        }
        return link;
    }

    /**
     * Creates a form with an input bound to the filter model
     *
     * @param id component id
     * @return filter form
     */
    private Form createFilterForm(String id) {
        final Form filterForm = new Form(id);
        final TextField<String> filterField = new AjaxIndicatingTextField<>("filterText", new PropertyModel<>(filterModel, "name"));
        // make field update 
        filterField.add(new AjaxFormComponentUpdatingBehavior("keyup") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                //update values
                target.add(facetValuesPanel);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                //enable throttling on the ajax behaviour - this causes 'debouncing' so that a
                //request is not sent on every keyup but only after a (short) interval
                //(This fixes https://github.com/clarin-eric/VLO/issues/215)
                attributes.setThrottlingSettings(new ThrottlingSettings(FILTER_RESPONSE_THROTTLE, true));
            }

        });
        filterField.add(new AttributeModifier("placeholder", new StringResourceModel("typeToFilter")));
        filterForm.add(filterField);
        filterForm.add(new AttributeModifier("title", new StringResourceModel("filterValues")));
        return filterForm;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        final boolean valuesSelected = !selectedFacetPanel.getModelObject().isEmpty();
        selectedFacetPanel.setVisible(valuesSelected);
        facetValuesPanel.setVisible(expansionModel.getObject() != ExpansionState.COLLAPSED);

        // hide this entire panel if nothing is selected or there is nothing to be selected
        setVisible(shouldShowFacet());
    }

    protected boolean shouldShowFacet() {
        final boolean hasSelection = !selectedFacetPanel.getModelObject().isEmpty();

        if (hasSelection) {
            // if there is a selection, always show
            return true;
        } else if (!meetsDisplayConditions()) {
            // if no selection and should NOT be shown according to display
            // conditions, do not show
            return false;
        } else {
            // no selection and no condition against showing;
            // is showing with no values allowed?
            if (!isHideIfNoValues()) {
                //then always show
                return true;
            }

            // if no selection, show facet IFF there are values to be shown
            if (facetValuesPanel.getModelObject() != null) {
                if (facetValuesPanel.getModelObject().getValueCount() > 0) {
                    return true;
                }
            }

            // else hide
            return false;
        }
    }

    /**
     * Override to add a preconditions check for showing this facet; if returns
     * false the facet will be hidden *unless* a value has been selected
     *
     * @return
     */
    protected boolean meetsDisplayConditions() {
        return true;
    }

    /**
     *
     * @return whether the panel should be hidden if no values are present given
     * the current selection and query
     */
    protected boolean isHideIfNoValues() {
        return true;
    }

    private FacetValuesPanel createFacetValuesPanel(String id, final IModel<String> facetNameModel, IModel<FacetField> facetFieldModel, final IModel<QueryFacetsSelection> selectionModel, int subListSize) {
        return (FacetValuesPanel) new FacetValuesPanel(id, facetFieldModel, selectionModel, selectionTypeModeModel, filterModel, subListSize) {
            @Override
            public void onValuesSelected(FacetSelectionType selectionType, Collection<String> values, Optional<AjaxRequestTarget> target) {
                if (selectionType != null && values != null) {
                    // A value has been selected on this facet's panel, update the model!
                    selectionModel.getObject().addNewFacetValue(facetNameModel.getObject(), selectionType, values);
                }

                if (target != null) {
                    // reload entire page for now
                    selectionChanged(target);
                }
            }
        }.setOutputMarkupId(true);
    }

    private SelectedFacetPanel createSelectedFacetPanel(String id, final IModel<String> facetNameModel, final IModel<QueryFacetsSelection> selectionModel) {
        return new SelectedFacetPanel(id, facetNameModel, new SelectionModel(facetNameModel, selectionModel)) {
            @Override
            public void onValuesUnselected(Collection<String> valuesRemoved, Optional<AjaxRequestTarget> target) {
                // Values have been removed, calculate remainder
                selectionModel.getObject().removeFacetValue(facetNameModel.getObject(), valuesRemoved);

                selectionChanged(target);
            }
        };
    }

    @Override
    protected void onExpansionToggle(Optional<AjaxRequestTarget> target) {
        super.onExpansionToggle(target);
        target.ifPresent(t -> {
            t.appendJavaScript("applyFacetTooltips();");
        });
    }

    @Override
    public void detachModels() {
        super.detachModels();

        if (selectionTypeModeModel != null) {
            this.selectionTypeModeModel.detach();
        }
        if (filterModel != null) {
            filterModel.detach();
        }
    }

    protected abstract void selectionChanged(Optional<AjaxRequestTarget> target);

}
