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

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermScriptFactory;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeGlyphicon;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import eu.clarin.cmdi.vlo.wicket.provider.ResouceTypeCountDataProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author twagoo
 */
public class SearchResultItemPanel extends Panel {

    @SpringBean
    private VloConfig config;
    @SpringBean
    private ResourceTypeCountingService countingService;

    private final IModel<SearchContext> selectionModel;
    private final IModel<SolrDocument> documentModel;

    private final Panel collapsedDetails;
    private final Panel expandedDetails;
    private final IModel<ExpansionState> expansionStateModel;

    /**
     *
     * @param id markup id of the panel
     * @param documentModel model of document that this search item represents
     * @param selectionModel model of current selection (will be passed on to
     * record page when link is clicked)
     * @param expansionStateModel model for the expansion state of this search
     * item
     * @param availabilityOrdering ordering for availability 'tags'
     */
    public SearchResultItemPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel, IModel<ExpansionState> expansionStateModel, Ordering<String> availabilityOrdering) {
        super(id, documentModel);
        this.expansionStateModel = expansionStateModel;
        this.selectionModel = selectionModel;
        this.documentModel = documentModel;

        final Link recordLink = new RecordPageLink("recordLink", documentModel, selectionModel);
        recordLink.add(new SolrFieldLabel("title", documentModel, FacetConstants.FIELD_NAME, "Unnamed record"));
        add(recordLink);

        // add a link to toggle the expansion state
        add(createExpansionStateToggle("expansionStateToggle"));

        // add a collapsed details panel; only shown when expansion state is collapsed (through onConfigure)
        collapsedDetails = new SearchResultItemCollapsedPanel("collapsedDetails", documentModel, selectionModel, availabilityOrdering);
        add(collapsedDetails);

        // add a collapsed details panel; only shown when expansion state is expanded (through onConfigure)
        expandedDetails = new SearchResultItemExpandedPanel("expandedDetails", documentModel, selectionModel, availabilityOrdering);
        add(expandedDetails);

        // get model for resources
        final SolrFieldModel<String> resourcesModel = new SolrFieldModel<>(documentModel, FacetConstants.FIELD_RESOURCE);
        // wrap with a count provider
        final ResouceTypeCountDataProvider countProvider = new ResouceTypeCountDataProvider(resourcesModel, countingService);

        // add a container for the resource type counts (only visible if there are actual resources)
        add(new WebMarkupContainer("resources")
                // view that shows provided counts
                .add(new ResourceCountDataView("resourceCount", countProvider))
                .add(new WebMarkupContainer("noResources") {

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(countProvider.size() == 0);
                    }
                }.add(new RecordPageLink("recordLink", documentModel, selectionModel)) //initial tab *not* resources as there are none...
                )
        );

        add(new SearchResultItemLicensePanel("licenseInfo", documentModel, selectionModel, availabilityOrdering));

        final MarkupContainer scoreContainer = new WebMarkupContainer("scoreContainer");
        scoreContainer.add(new Label("score", new SolrFieldStringModel(documentModel, FacetConstants.FIELD_SOLR_SCORE)));
        scoreContainer.setVisible(config.isShowResultScores());
        add(scoreContainer);

        setOutputMarkupId(true);
    }

    private Link createExpansionStateToggle(String id) {
        final Link expansionStateToggle = new IndicatingAjaxFallbackLink(id) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                // toggle the expansion state
                if (expansionStateModel.getObject() == ExpansionState.COLLAPSED) {
                    expansionStateModel.setObject(ExpansionState.EXPANDED);
                } else {
                    expansionStateModel.setObject(ExpansionState.COLLAPSED);
                }

                if (target != null) {
                    // parial update (just this search result item)
                    target.add(SearchResultItemPanel.this);

                    // in case of a query, update highlight matching search terms after collapse/expand
                    final String query = selectionModel.getObject().getSelection().getQuery();
                    if (!Strings.isEmpty(query)) {
                        final HighlightSearchTermScriptFactory scriptFactory = new HighlightSearchTermScriptFactory();
                        final String selector = "#" + SearchResultItemPanel.this.getMarkupId();
                        target.appendJavaScript(scriptFactory.createScript(selector, query));
                    }
                }
            }
        };
        expansionStateToggle.add(
                new WebMarkupContainer("state").add(
                        new AttributeModifier("class", new AbstractReadOnlyModel<String>() {

                            @Override
                            public String getObject() {
                                if (expansionStateModel.getObject() == ExpansionState.COLLAPSED) {
                                    return "fa fa-plus-square-o";
                                } else {
                                    return "fa fa-minus-square-o";
                                }
                            }
                        })));
        return expansionStateToggle;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        // this is called once per request; set visibility state for detail panels
        // according to expansion state
        collapsedDetails.setVisible(expansionStateModel.getObject() == ExpansionState.COLLAPSED);
        expandedDetails.setVisible(expansionStateModel.getObject() == ExpansionState.EXPANDED);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        expansionStateModel.detach();
    }

    /**
     * Data view for resource type counts coming from a data provider for
     * {@link ResourceTypeCount}
     */
    private class ResourceCountDataView extends DataView<ResourceTypeCount> {

        public ResourceCountDataView(String id, IDataProvider<ResourceTypeCount> dataProvider) {
            super(id, dataProvider);
        }

        @Override
        protected void populateItem(Item<ResourceTypeCount> item) {
            final Link resourceLink = new RecordPageLink("recordLink", documentModel, selectionModel, RecordPage.RESOURCES_SECTION);
            item.add(resourceLink
                    .add(new Label("resourceCountLabel", new PropertyModel<String>(item.getModel(), "count")))
                    .add(new ResourceTypeGlyphicon("resourceTypeIcon", new PropertyModel<String>(item.getModel(), "resourceType")))
                    .add(new AttributeModifier("title", getResourceCountModel(item.getModel())))
            );
        }

        /**
         *
         * @param resourceTypeCountModel
         * @return a string model that conveys the type of resource and number
         * of instances
         */
        private IModel<String> getResourceCountModel(final IModel<ResourceTypeCount> resourceTypeCountModel) {
            // first create a string model that provides the type of resources 
            // in the right number (plural or singular, conveniently supplied by ResourceTypeCount)
            final StringResourceModel resourceTypeModel = StringResourceModelMigration.of("resourcetype.${resourceType}.${number}", resourceTypeCountModel, "?");
            // inject this into the resource string that combines it with count
            return StringResourceModelMigration.of("resources.typecount", this, resourceTypeCountModel, resourceTypeModel);
        }
    }
}
