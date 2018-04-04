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

import com.google.common.collect.ImmutableSet;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.NameAndCountFieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior.EventTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior.FacetValueSelectionTrackingBehaviour;
import eu.clarin.cmdi.vlo.wicket.components.FieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.provider.PartitionedDataProvider;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.pages.AllFacetValuesPage;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.wicket.panels.BootstrapModal;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel representing a single facet and its selectable values
 *
 * @author twagoo
 */
public abstract class FacetValuesPanel extends GenericPanel<FacetField> {

    public final static int MAX_NUMBER_OF_FACETS_TO_SHOW = 10; //TODO: get from config
    public final static Collection<String> LOW_PRIORITY_VALUES = ImmutableSet.of("unknown", "unspecified", "");

    private final BootstrapModal valuesWindow;
    private final IModel<QueryFacetsSelection> selectionModel;
    private final WebMarkupContainer valuesContainer;
    private final IModel<FieldValuesFilter> filterModel;
    private final int subListSize;
    private final IModel<String> fieldNameModel;
    private final IModel<FacetSelectionType> selectionTypeModeModel;
    private final IModel<QueryFacetsSelection> beforeAllValuesSelection = new Model<>();
    private final IModel<FieldValuesFilter> beforeAllValuesFilter = new Model<>();

    @SpringBean
    private FieldValueConverterProvider fieldValueConverterProvider;

    @SpringBean
    private PiwikConfig piwikConfig;

    /**
     * Creates a new panel with selectable values for a single facet
     *
     * @param id component id
     * @param model facet field model for this panel
     * @param selectionModel model holding the global query/facet selection
     * @param selectionTypeModel model holding the current selection type
     * @param filterModel model for facet value filter/search text
     */
    public FacetValuesPanel(String id, final IModel<FacetField> model, final IModel<QueryFacetsSelection> selectionModel, final IModel<FacetSelectionType> selectionTypeModel, IModel<FieldValuesFilter> filterModel) {
        this(id, model, selectionModel, selectionTypeModel, filterModel, 0);
    }

    /**
     * Creates a new panel with selectable values for a single facet
     *
     * @param id component id
     * @param model facet field model for this panel
     * @param selectionModel model holding the global query/facet selection
     * @param selectionTypeModel model holding the current selection type
     * @param filterModel model for facet value filter/search text
     * @param subListSize if large than 0, multiple lists will be generated each
     * with a maximum size of this value
     */
    public FacetValuesPanel(String id, final IModel<FacetField> model, final IModel<QueryFacetsSelection> selectionModel, final IModel<FacetSelectionType> selectionTypeModel, IModel<FieldValuesFilter> filterModel, int subListSize) {
        super(id, model);
        this.selectionModel = selectionModel;
        this.selectionTypeModeModel = selectionTypeModel;
        this.filterModel = filterModel;
        this.subListSize = subListSize;

        // create a container for values to allow for AJAX updates when filtering
        valuesContainer = new WebMarkupContainer("valuesContainer");
        valuesContainer.setOutputMarkupId(true);
        add(valuesContainer);

        // create a view for the actual values
        valuesContainer.add(createValuesView("valuesList"));

        // create a link for showing all values        
        valuesContainer.add(createAllValuesLink("allFacetValuesLink"));

        // create a popup window for all facet values
        valuesWindow = createAllValuesWindow("allValues");
        add(valuesWindow);

        fieldNameModel = new PropertyModel<>(model, "name");
    }

    /**
     * Creates a view for the actual values (as links) for selection
     *
     * @param id component id
     * @return data view with value links
     */
    private DataView createValuesView(String id) {
        final FacetFieldValuesProvider valuesProvider = new FacetFieldValuesProvider(getModel(), MAX_NUMBER_OF_FACETS_TO_SHOW, LOW_PRIORITY_VALUES, fieldValueConverterProvider) {

            @Override
            protected IModel<FieldValuesFilter> getFilterModel() {
                return filterModel;
            }

        };
        // partition the values according to the specified partition size
        final PartitionedDataProvider<Count, FieldValuesOrder> partitionedValuesProvider = new PartitionedDataProvider<>(valuesProvider, subListSize);

        // create the view for the partitions
        final DataView<List<Count>> valuesView = new DataView<List<Count>>(id, partitionedValuesProvider) {

            @Override
            protected void populateItem(Item<List<Count>> item) {
                // create a list view for the values in this partition
                item.add(new ListView("facetValues", item.getModel()) {

                    @Override
                    protected void populateItem(ListItem item) {
                        addFacetValue("facetSelect", item);
                    }
                });
            }
        };
        valuesView.setOutputMarkupId(true);
        return valuesView;
    }

    /**
     * Adds an individual facet value selection link to a dataview item
     *
     * @param item item to add link to
     */
    private void addFacetValue(String id, final ListItem<Count> item) {
        item.setDefaultModel(new CompoundPropertyModel<>(item.getModel()));

        final FacetValueSelectionTrackingBehaviour selectionTrackingBehavior;
        if (piwikConfig.isEnabled()) {
            selectionTrackingBehavior = new FacetValueSelectionTrackingBehaviour(PiwikEventConstants.PIWIK_EVENT_ACTION_FACET_SELECT, fieldNameModel, new PropertyModel<String>(item.getModel(), "name"));
        } else {
            selectionTrackingBehavior = null;
        }

        // link to select an individual facet value
        final Link selectLink = new IndicatingAjaxFallbackLink(id) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                // reset filter
                ((NameAndCountFieldValuesFilter) filterModel.getObject()).setName(null);

                // call callback
                onValuesSelected(
                        //TODO: get type injected via model
                        selectionTypeModeModel.getObject(),
                        // for now only single values can be selected
                        Collections.singleton(item.getModelObject().getName()),
                        target);

                if (target != null && selectionTrackingBehavior != null) {
                    target.appendJavaScript(selectionTrackingBehavior.generatePiwikJs(target));
                }
            }
        };
        item.add(selectLink);

        // 'name' field from Count (name of value)
        selectLink.add(new FieldValueLabel("name", fieldNameModel));
        // 'count' field from Count (document count for value)
        selectLink.add(new Label("count"));
    }

    /**
     * Creates a link that leads to the 'all facet values' view, either as a
     * modal window (if JavaScript is enabled, see {@link #createAllValuesWindow(java.lang.String)
     * }) or by redirecting to {@link AllFacetValuesPage}
     *
     * @param id component id
     * @return 'show all values' link
     */
    private Link createAllValuesLink(String id) {
        final EventTrackingBehavior allValuesTrackingBehaviour;
        if (piwikConfig.isEnabled()) {
            allValuesTrackingBehaviour = new EventTrackingBehavior("click", PiwikEventConstants.PIWIK_EVENT_CATEGORY_FACET, PiwikEventConstants.PIWIK_EVENT_ACTION_FACET_ALLVALUES) {
                @Override
                protected String getName(AjaxRequestTarget target) {
                    return getModel().getObject().getName();
                }

            };
        } else {
            allValuesTrackingBehaviour = null;
        }

        final Link link = new IndicatingAjaxFallbackLink<FacetField>(id, getModel()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (target == null) {
                    // no JavaScript, open a new page with values
                    setResponsePage(new AllFacetValuesPage(getModel(), selectionModel, selectionTypeModeModel));
                } else {
                    // JavaScript enabled, show values in a modal popup. First store copy of current selection to allow the user to cancel.
                    beforeAllValuesSelection.setObject(selectionModel.getObject().copy());
                    if (filterModel.getObject() == null) {
                        beforeAllValuesFilter.setObject(null);
                    } else {
                        beforeAllValuesFilter.setObject(filterModel.getObject().copy());
                    }
                    valuesWindow.show(target);
                    if (allValuesTrackingBehaviour != null) {
                        target.appendJavaScript(allValuesTrackingBehaviour.generatePiwikJs(target));
                    }
                }
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only show if there actually are more values!
                setVisible(getModelObject().getValueCount() > MAX_NUMBER_OF_FACETS_TO_SHOW);
            }

        };
        return link;
    }

    /**
     * Creates a modal window showing a {@link AllFacetValuesPanel} for this
     * facet
     *
     * @param id component id
     * @return 'all facet values' modal window component
     */
    private BootstrapModal createAllValuesWindow(String id) {
        final BootstrapModal window = new BootstrapModal(id) {

            @Override
            public IModel<String> getTitle() {
                return new SolrFieldNameModel(getModel(), "name");
            }

            @Override
            protected IModel<?> getCloseButtonLabelModel() {
                return Model.of("Apply");
            }
            @Override
            protected IModel<?> getDismissButtonLabelModel() {
                return Model.of("Cancel");
            }

            private void updateAfterClose(AjaxRequestTarget target) {
                onValuesSelected(null, null, target);
            }

            @Override
            protected void onDismiss(AjaxRequestTarget target) {
                final QueryFacetsSelection previousSelection = beforeAllValuesSelection.getObject();
                if (previousSelection != null) {
                    selectionModel.setObject(previousSelection);
                }
                filterModel.setObject(beforeAllValuesFilter.getObject());
                close(target);
                updateAfterClose(target);
            }

            @Override
            protected void onClose(AjaxRequestTarget target) {
                close(target);
                filterModel.setObject(beforeAllValuesFilter.getObject());
                updateAfterClose(target);
            }

        };

        final Component modalContent = new AllFacetValuesPanel(window.getContentId(), getModel(), selectionTypeModeModel, selectionModel, filterModel) {
            @Override
            protected void onSelectionChanged(AjaxRequestTarget target) {
                if (target != null) {
                    // Special case: update search results only so as to provide some visual feedback. 
                    // (calling onValuesSelected would be nice but may re-render the facet panels which would break the modal window)
                    final Page page = getPage();
                    if (page instanceof FacetedSearchPage) {
                        final Component resultsPanel = ((FacetedSearchPage)page).getSearchResultsPanel();
                        if (resultsPanel != null) {
                            target.add(resultsPanel);
                        }
                    }
                }
            }

        };

        window.addOrReplace(modalContent);
        window.setShowDismissIcon(false);
        window.setShowDismissButton(true);
        return window;
    }

    @Override
    public void detachModels() {
        super.detachModels();

        if (selectionModel != null) {
            selectionModel.detach();
        }

        if (selectionTypeModeModel != null) {
            this.selectionTypeModeModel.detach();
        }

        if (filterModel != null) {
            filterModel.detach();
        }
    }

    /**
     * Callback triggered when values have been selected on this facet
     *
     * @param selectionType
     * @param values selected values
     * @param target Ajax target allowing for a partial update. May be null
     * (fallback)!
     */
    protected abstract void onValuesSelected(FacetSelectionType selectionType, Collection<String> values, AjaxRequestTarget target);
}
