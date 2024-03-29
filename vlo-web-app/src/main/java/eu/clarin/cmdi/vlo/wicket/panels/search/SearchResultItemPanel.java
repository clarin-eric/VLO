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

import eu.clarin.cmdi.vlo.wicket.LandingPageShortLinkLabelConverter;
import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.ResourceAvailabilityScore;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import eu.clarin.cmdi.vlo.pojo.ResourceTypeCount;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.service.ResourceTypeCountingService;
import eu.clarin.cmdi.vlo.wicket.AddToVcrQueueButtonBehavior;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.HighlightSearchTermScriptFactory;
import eu.clarin.cmdi.vlo.wicket.components.FacetSelectLink;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.ResourceAvailabilityWarningBadge;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.components.SingleValueSolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.model.ActionableLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrDocumentExpansionPairModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import eu.clarin.cmdi.vlo.wicket.model.IsPidModel;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.RecordMetadataLinksCountModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.TruncatingStringModel;
import eu.clarin.cmdi.vlo.wicket.provider.ResouceTypeCountDataProvider;
import java.util.Collection;
import java.util.Optional;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;

/**
 *
 * @author twagoo
 */
public class SearchResultItemPanel extends Panel {

    private final static IConverter<String> landingPageLabelConverter = new LandingPageShortLinkLabelConverter();

    @SpringBean
    private VloConfig config;
    @SpringBean
    private PiwikConfig piwikConfig;
    @SpringBean
    private ResourceTypeCountingService countingService;
    @SpringBean
    private FieldNameService fieldNameService;
    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;

    private final IModel<SearchContext> selectionModel;
    private final IModel<SolrDocument> documentModel;

    private final Panel collapsedDetails;
    private final Panel expandedDetails;
    private final IModel<ExpansionState> expansionStateModel;

    /**
     *
     * @param id markup id of the panel
     * @param documentExpansionPairModel model of document that this search item
     * represents
     * @param selectionModel model of current selection (will be passed on to
     * record page when link is clicked)
     * @param expansionStateModel model for the expansion state of this search
     * item
     * @param availabilityOrdering ordering for availability 'tags'
     */
    public SearchResultItemPanel(String id, SolrDocumentExpansionPairModel documentExpansionPairModel, IModel<SearchContext> selectionModel, IModel<ExpansionState> expansionStateModel, IModel<ExpansionState> duplicateItemsExpansionModel, Ordering<String> availabilityOrdering, IModel<ExpansionState> languageExpansionStateModel) {
        super(id, documentExpansionPairModel);
        this.expansionStateModel = expansionStateModel;
        this.selectionModel = selectionModel;
        this.documentModel = new PropertyModel<>(documentExpansionPairModel, "document");

        // part count model to determine whether a record is a collection record
        final RecordMetadataLinksCountModel recordMetadataLinksCountModel = new RecordMetadataLinksCountModel(documentModel);

        add(new RecordPageLink("recordLink", documentModel, selectionModel)
                .add(new SingleValueSolrFieldLabel("title", documentModel, fieldNameService.getFieldName(FieldKey.NAME), new StringResourceModel("searchpage.unnamedrecord", this)))
                // add icon to title for collection records
                .add(new WebMarkupContainer("titleCollectionRecordIcon")
                        .add(BooleanVisibilityBehavior.visibleOnTrue(() -> recordMetadataLinksCountModel.getObject() > 0)))
        );

        add(new FacetSelectLink("searchResultCollectionLink", new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.COLLECTION)), Model.of(fieldNameService.getFieldName(FieldKey.COLLECTION)))
                .add(new SolrFieldLabel("searchResultCollectionName", documentModel, fieldNameService.getFieldName(FieldKey.COLLECTION), "none"))
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(documentModel.getObject().getFieldValue(fieldNameService.getFieldName(FieldKey.COLLECTION)) != null);
                    }

                })
        );

        // add a link to toggle the expansion state
        add(createExpansionStateToggle("expansionStateToggle"));

        // add a collapsed details panel; only shown when expansion state is collapsed (through onConfigure)
        collapsedDetails = new SearchResultItemCollapsedPanel("collapsedDetails", documentModel, selectionModel, availabilityOrdering, languageExpansionStateModel);
        add(collapsedDetails);

        // add a collapsed details panel; only shown when expansion state is expanded (through onConfigure)
        expandedDetails = new SearchResultItemExpandedPanel("expandedDetails", documentModel, selectionModel, availabilityOrdering);
        add(expandedDetails);

        // get model for resources
        final SolrFieldModel<String> resourcesModel = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.RESOURCE));
        // wrap with a count provider
        final ResouceTypeCountDataProvider countProvider = new ResouceTypeCountDataProvider(resourcesModel, countingService);

        final SolrFieldModel<Integer> resourceAvailabilityScoreModel = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.RESOURCE_AVAILABILITY_SCORE));
        final IModel<Boolean> resourceAvailabilityWarningModel = new LoadableDetachableModel<Boolean>() {
            @Override
            public Boolean load() {
                return Optional.ofNullable(resourceAvailabilityScoreModel.getObject())
                        .map(Collection::iterator)
                        .flatMap(i -> (i.hasNext() ? Optional.of(i.next() < ResourceAvailabilityScore.UNKNOWN.getScoreValue()) : Optional.empty()))
                        .orElse(false);
            }
        };

        final IModel<Boolean> restrictedAccessWarningModel = new LoadableDetachableModel<Boolean>() {
            @Override
            public Boolean load() {
                return Optional.ofNullable(resourceAvailabilityScoreModel.getObject())
                        .map(Collection::iterator)
                        .flatMap(i -> (i.hasNext() ? Optional.of(i.next() >= ResourceAvailabilityScore.MOST_RESTRICTED_ACCESS.getScoreValue()) : Optional.empty()))
                        .orElse(false);
            }
        };

        final ResourceAvailabilityWarningBadge resourceAvailabilityWarningBadge
                = new ResourceAvailabilityWarningBadge("warningBadge", resourceAvailabilityWarningModel, restrictedAccessWarningModel) {
            @Override
            protected IModel<String> getResourceUnavailableTooltip() {
                return Model.of("One or more of the linked resources may not be unavailable or have restricted access");
            }

            @Override
            protected IModel<String> getResourceRestrictedTooltip() {
                return Model.of("One or more of the linked resources may have restricted access");
            }

        };

        // add a container for the resource type counts (only visible if there are actual resources)
        add(new WebMarkupContainer("resources")
                // view that shows provided counts
                .add(new ResourceCountDataView("resourceCount", countProvider))
                //badge for collection records
                .add(new WebMarkupContainer("collectionRecord")
                        // collection, go to hierarchy instead of records
                        .add(new RecordPageLink("recordLink", documentModel, selectionModel, RecordPage.HIERARCHY_SECTION)
                                .add(new Label("hierarchyChildrenCountLabel", recordMetadataLinksCountModel))
                                .add(new AttributeModifier("title", new StringResourceModel("searchresult.hierarchy.badgetitle", recordMetadataLinksCountModel))))
                        //badge for records without resources (resource count data view will not yield any badges)
                        .add(BooleanVisibilityBehavior.visibleOnTrue(() -> recordMetadataLinksCountModel.getObject() > 0))
                )
                //badge for record with no resources
                .add(new WebMarkupContainer("noResources")
                        .add(new RecordPageLink("recordLink", documentModel, selectionModel)) //initial tab *not* resources as there are none...
                        .add(BooleanVisibilityBehavior.visibleOnTrue(() -> countProvider.size() == 0 && recordMetadataLinksCountModel.getObject() <= 0))
                )
                //badge for availability warning
                .add(new WebMarkupContainer("availabilityWarning")
                        .add(new RecordPageLink("recordLink", documentModel, selectionModel, RecordPage.RESOURCES_SECTION)
                                .add(resourceAvailabilityWarningBadge)
                        )
                        .add(BooleanVisibilityBehavior.visibleOnTrue(resourceAvailabilityWarningModel)))
        );

        add(new SearchResultItemLicensePanel("licenseInfo", documentModel, selectionModel, availabilityOrdering));
        add(createAddToVcrQueueLink("addToVcrQueueLink")
        );

        add(new WebMarkupContainer("scoreContainer")
                .add(new Label("score", new SolrFieldStringModel(documentModel, fieldNameService.getFieldName(FieldKey.SOLR_SCORE))))
                .setVisible(config.isShowResultScores())
        );

        add(new DuplicateSearchResultItemsPanel("duplicateResults", documentExpansionPairModel, new PropertyModel<>(selectionModel, "selection"), duplicateItemsExpansionModel));

        add(createLandingPageLinkContainer("landingPageLinkContainer", documentModel));

        setOutputMarkupId(true);
    }

    private Component createAddToVcrQueueLink(String id) {
        final Component link
                = new WebMarkupContainer(id)
                        .add(new AddToVcrQueueButtonBehavior(documentModel));
        if (piwikConfig.isEnabled()) {
            final AjaxPiwikTrackingBehavior.EventTrackingBehavior eventBehavior = new AjaxPiwikTrackingBehavior.EventTrackingBehavior("click", PiwikEventConstants.PIWIK_EVENT_CATEGORY_VCR, PiwikEventConstants.PIWIK_EVENT_ACTION_VCR_ADD_TO_QUEUE) {
                @Override
                protected String getName(AjaxRequestTarget target) {
                    return "SearchResultItem";
                }

                @Override
                protected String getValue(AjaxRequestTarget target) {
                    return String.valueOf(documentModel.getObject().getFieldValue(fieldNameService.getFieldName(FieldKey.SELF_LINK)));
                }
            };
            link.add(eventBehavior);
        }
        return link;
    }

    private Link createExpansionStateToggle(String id) {
        final Link expansionStateToggle = new IndicatingAjaxFallbackLink<Void>(id) {

            @Override
            public void onClick(Optional<AjaxRequestTarget> t) {
                // toggle the expansion state
                if (expansionStateModel.getObject() == ExpansionState.COLLAPSED) {
                    expansionStateModel.setObject(ExpansionState.EXPANDED);
                } else {
                    expansionStateModel.setObject(ExpansionState.COLLAPSED);
                }
                t.ifPresent(target -> {
                    // parial update (just this search result item)
                    target.add(SearchResultItemPanel.this);

                    // in case of a query, update highlight matching search terms after collapse/expand
                    final String query = selectionModel.getObject().getSelection().getQuery();
                    if (!Strings.isEmpty(query)) {
                        final HighlightSearchTermScriptFactory scriptFactory = new HighlightSearchTermScriptFactory();
                        final String selector = "#" + SearchResultItemPanel.this.getMarkupId();
                        target.appendJavaScript(scriptFactory.createScript(selector, query));
                    }
                });
            }
        };
        expansionStateToggle.add(
                new WebMarkupContainer("state").add(
                        new AttributeModifier("class", new IModel<>() {

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

    private Component createLandingPageLinkContainer(String id, IModel<SolrDocument> documentModel) {
        final String landingPageField = fieldNameService.getFieldName(FieldKey.LANDINGPAGE);
        final IModel<String> landingPageModel = new SolrFieldStringModel(documentModel, landingPageField);
        final ResourceInfoModel landingPageResourceInfoModel = new ResourceInfoModel(resourceStringConverter, landingPageModel);
        final IModel<String> landingPageLinkModel = new PropertyModel(landingPageResourceInfoModel, "href");
        final IModel<Boolean> isPidModel = new IsPidModel(landingPageLinkModel);

        final ResourceAvailabilityWarningBadge resourceAvailabilityWarningBadge = new ResourceAvailabilityWarningBadge("warningBadge", landingPageResourceInfoModel) {
            @Override
            protected IModel<String> getResourceRestrictedTooltip() {
                return Model.of("Authentication and/or special permissions may be required in order to access the resource. See record page for details.");
            }

            @Override
            protected IModel<String> getResourceUnavailableTooltip() {
                return Model.of("The resource may not be available at this location. See record page for details.");
            }

        };

        return new WebMarkupContainer(id)
                .add(new ExternalLink("landingPageLink", new PIDLinkModel(landingPageLinkModel))
                        .add(resourceAvailabilityWarningBadge)
                        .add(new WebMarkupContainer("landingPagePidLabel")
                                .add(BooleanVisibilityBehavior.visibleOnTrue(isPidModel))
                        )
                        .add(new Label("landingPageLinkLabel", landingPageLinkModel) {
                            @Override
                            public <C> IConverter<C> getConverter(Class<C> type) {
                                if (type.equals(String.class)) {
                                    return (IConverter<C>) landingPageLabelConverter;
                                } else {
                                    return super.getConverter(type);
                                }
                            }

                        }.add(BooleanVisibilityBehavior.visibleOnFalse(isPidModel)))
                )
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(documentModel.getObject().containsKey(landingPageField));
                    }

                });
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
                    .add(new ResourceTypeIcon("resourceTypeIcon", new PropertyModel<>(item.getModel(), "resourceType")))
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
            // inject this into the resource string that combines it with count
            return new StringResourceModel("resources.typecount", this, resourceTypeCountModel)
                    .setParameters(
                            //wrap inside a string model that provides the type of resources in the
                            //right number (plural or singular, conveniently supplied by ResourceTypeCount)
                            new StringResourceModel("resourcetype.${resourceType}.${number}", resourceTypeCountModel)
                                    .setDefaultValue("?"));
        }
    }
}
