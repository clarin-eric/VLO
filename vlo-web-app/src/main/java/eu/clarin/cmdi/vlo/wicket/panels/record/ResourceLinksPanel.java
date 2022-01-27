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
package eu.clarin.cmdi.vlo.wicket.panels.record;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.pojo.ResourceType;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.NotNullModel;
import eu.clarin.cmdi.vlo.wicket.model.RecordMetadataLinksCountModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import static eu.clarin.cmdi.vlo.wicket.pages.RecordPage.HIERARCHY_SECTION;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that shows all resources represented by a collection of resource
 * strings as links that trigger a details panel for the selected resource
 *
 * @author twagoo
 */
public abstract class ResourceLinksPanel extends GenericPanel<SolrDocument> {

    private static final int ITEMS_PER_PAGE = 12;

    @SpringBean
    private VloConfig config;
    @SpringBean
    private FieldNameService fieldNameService;
    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;

    private final IModel<List<String>> detailsVisibleModel = new ListModel<>(new ArrayList<>());
    private final WebMarkupContainer resourcesTable;
    private final ResourcesListView resourceListing;
    private final IModel<List<String>> landingPagesLinkModel;
    private final IModel<List<String>> searchPagesLinkModel;
    private final IModel<String> searchServiceLinkModel;

    /**
     *
     * @param id panel id
     * @param documentModel model of document that holds the resources
     */
    public ResourceLinksPanel(String id, IModel<SolrDocument> documentModel) {
        super(id, documentModel);

        final boolean enableFcsLinks = config.isEnableFcsLinks();

        // create table of resources with optional details
        resourcesTable = new WebMarkupContainer("resources") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(resourceListing.getPageCount() > 0
                        || landingPagesLinkModel.getObject() != null
                        || searchPagesLinkModel.getObject() != null
                        || (enableFcsLinks && searchServiceLinkModel.getObject() != null));
            }

        };
        resourcesTable.setOutputMarkupId(true);
        add(resourcesTable);

        //add the 'actual' resources listing
        final SolrFieldModel<String> resourcesModel
                = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.RESOURCE));
        resourceListing = new ResourcesListView("resource", new CollectionListModel<>(resourcesModel));
        resourcesTable.add(resourceListing);

        // special items in table for landing page, search page and search service 'resources'
        landingPagesLinkModel = new CollectionListModel<>(new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.LANDINGPAGE)));
        searchPagesLinkModel = new CollectionListModel<>(new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.SEARCHPAGE)));
        if (enableFcsLinks) {
            searchServiceLinkModel = new SolrFieldStringModel(getModel(), fieldNameService.getFieldName(FieldKey.SEARCH_SERVICE));
        } else {
            searchServiceLinkModel = new Model<>();
        }

        final RecordMetadataLinksCountModel childRecordCountModel = new RecordMetadataLinksCountModel(documentModel);

        resourcesTable
                .add(createSearchPageItems("searchPageItems").add(specialLinkVisibilityBehavior(NotNullModel.of(searchPagesLinkModel))))
                .add(createSearchServiceItem(documentModel).add(specialLinkVisibilityBehavior(NotNullModel.of(searchServiceLinkModel))))
                .add(createLandingPageItems("landingPageItems").add(specialLinkVisibilityBehavior(NotNullModel.of(landingPagesLinkModel))))
                .add(createHierarchyItem(childRecordCountModel).add(specialLinkVisibilityBehaviorLastPage(() -> childRecordCountModel.getObject() > 0)));

        // pagination
        add(new BootstrapAjaxPagingNavigator("paging", resourceListing) {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(resourceListing.getPageCount() > 1);
            }

        });

        // panel for records with no resources
        add(createNoResourcesContainer("noResources"));

        //For Ajax updating of resource listing when paging
        setOutputMarkupId(true);
    }

    private SpecialLinkItemsView createSearchPageItems(String id) {
        final SpecialLinkItemsView searchPageItems = new SpecialLinkItemsView(id, "search page", ResourceTypeIcon.SEARCH_PAGE, searchPagesLinkModel) {
            @Override
            protected IModel<ResourceInfo> createInfoModel(IModel<String> linkModel) {
                return Model.of(new ResourceInfo(linkModel.getObject(), "Search page for this record", null, null, null, ResourceType.OTHER));
            }

            @Override
            protected boolean showDetailsLink() {
                return false;
            }

        };
        return searchPageItems;
    }

    private ResourceLinksPanelSearchServiceItem createSearchServiceItem(IModel<SolrDocument> documentModel) {
        return new ResourceLinksPanelSearchServiceItem("searchServiceItem", searchServiceLinkModel, documentModel);
    }

    private ResourceLinksPanelHierarchyItem createHierarchyItem(IModel<Integer> childRecordCountModel) {
        return new ResourceLinksPanelHierarchyItem("hierarchyItem", childRecordCountModel) {
            @Override
            protected void switchToHierarchyTab(Optional<AjaxRequestTarget> target) {
                switchToTab(HIERARCHY_SECTION, target);
            }
        };
    }

    private SpecialLinkItemsView createLandingPageItems(String id) {
        final SpecialLinkItemsView landingPageItems = new SpecialLinkItemsView(id, "landing page", ResourceTypeIcon.LANDING_PAGE, landingPagesLinkModel) {
            @Override
            protected IModel<ResourceInfo> createInfoModel(IModel<String> linkModel) {
                return new ResourceInfoModel(resourceStringConverter, linkModel);
            }

        };
        return landingPageItems;
    }

    private Behavior specialLinkVisibilityBehavior(final IModel<Boolean> linkModel) {
        return BooleanVisibilityBehavior.visibleOnTrue(
                () -> linkModel.getObject() && resourceListing.getCurrentPage() == 0);
    }

    private Behavior specialLinkVisibilityBehaviorLastPage(final IModel<Boolean> linkModel) {
        return BooleanVisibilityBehavior.visibleOnTrue(
                () -> linkModel.getObject() && resourceListing.getCurrentPage() + 1 == resourceListing.getPageCount());
    }

    /**
     * Creates a container to show in case of no resource links
     *
     * @param id
     * @return
     */
    private MarkupContainer createNoResourcesContainer(String id) {
        final MarkupContainer container = new WebMarkupContainer(id);

        //hierarchy link
        final SolrFieldModel<String> partCountModel
                = new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT));
        container.add(new WebMarkupContainer("hierarchyLinkContainer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(partCountModel.getObject() != null);
            }
        }.add(new AjaxFallbackLink<Void>("hierarchyLink") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                switchToTab(HIERARCHY_SECTION, target);
            }
        }));

        container.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);
                component.setVisible(resourceListing.getItemCount() == 0
                        && landingPagesLinkModel.getObject() == null
                        && searchPagesLinkModel.getObject() == null
                        && searchServiceLinkModel.getObject() == null
                );
            }
        });

        return container;
    }

    /**
     * View for a record's resource refs
     */
    private class ResourcesListView extends PageableListView<String> {

        public ResourcesListView(String id, IModel<? extends List<String>> model) {
            super(id, model, ITEMS_PER_PAGE);
            setReuseItems(true);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(resourceStringConverter, item.getModel());

            //detailed properties?
            final IModel<Boolean> itemDetailsShownModel = new IModel<>() {
                @Override
                public Boolean getObject() {
                    return detailsVisibleModel.getObject().contains(resourceInfoModel.getObject().getHref());
                }
            };

            item.add(new ResourceLinksPanelItem("resourceItem", resourceInfoModel, ResourceLinksPanel.this.getModel(), itemDetailsShownModel) {
                @Override
                protected void onDetailsToggleClick(String id, Optional<AjaxRequestTarget> target) {
                    final List<String> visible = detailsVisibleModel.getObject();
                    if (visible.contains(id)) {
                        visible.remove(id);
                    } else {
                        visible.add(id);
                    }

                    target.ifPresent(t -> {
                        t.add(resourcesTable);
                    });
                }

            });
        }
    }

    protected abstract void switchToTab(String tab, Optional<AjaxRequestTarget> target);

    private abstract class SpecialLinkItemsView extends ListView<String> {

        private final String label;
        private final String icon;

        public SpecialLinkItemsView(final String id, final String label, final String icon, final IModel<List<String>> pageLinksModel) {
            super(id, pageLinksModel);
            this.label = label;
            this.icon = icon;
            setReuseItems(true);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            final IModel<ResourceInfo> pageInfoModel = createInfoModel(item.getModel());
            final IModel<Boolean> detailsVisibilityModel = Model.of(Boolean.FALSE);
            item.add(new ResourceLinksSpecialItem("item", Model.of(label), Model.of(icon), pageInfoModel, ResourceLinksPanel.this.getModel(), detailsVisibilityModel) {
                @Override
                protected void onDetailsToggleClick(String id, Optional<AjaxRequestTarget> target) {
                    detailsVisibilityModel.setObject(!detailsVisibilityModel.getObject());

                    target.ifPresent(t -> {
                        t.add(resourcesTable);
                    });
                }

            }.setShowDetailsLink(showDetailsLink()));
        }

        protected abstract IModel<ResourceInfo> createInfoModel(IModel<String> linkModel);

        protected boolean showDetailsLink() {
            return true;
        }
    }

}
