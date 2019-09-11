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
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import static eu.clarin.cmdi.vlo.wicket.pages.RecordPage.HIERARCHY_SECTION;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.wicket.InvisibleIfNullBehaviour;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoObjectModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import java.util.Optional;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;

/**
 * Panel that shows all resources represented by a collection of resource
 * strings as links that trigger a details panel for the selected resource
 *
 * @author twagoo
 */
public abstract class ResourceLinksPanel extends GenericPanel<SolrDocument> {

    private final static Logger logger = LoggerFactory.getLogger(ResourceLinksPanel.class);

    private static final int ITEMS_PER_PAGE = 12;

    @SpringBean
    private FieldNameService fieldNameService;
    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;

    private final IModel<List<String>> detailsVisibleModel = new ListModel<>(new ArrayList<>());
    private final WebMarkupContainer resourcesTable;
    private final ResourcesListView resourceListing;

    /**
     *
     * @param id panel id
     * @param documentModel model of document that holds the resources
     */
    public ResourceLinksPanel(String id, IModel<SolrDocument> documentModel) {
        super(id, documentModel);

        // create table of resources with optional details
        resourcesTable = new WebMarkupContainer("resources") {
            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() > 0);
            }

        };
        resourcesTable.setOutputMarkupId(true);
        add(resourcesTable);

        // special item in table for landing page 'resource'
        final ResourceLinksPanelItem landingPageItem = createLandingPageItem("landingPageItem");
        resourcesTable.add(landingPageItem);

        //add the 'actual' resources listing
        final SolrFieldModel<String> resourcesModel
                = new SolrFieldModel<>(documentModel, fieldNameService.getFieldName(FieldKey.RESOURCE));
        resourceListing = new ResourcesListView("resource", new CollectionListModel<>(resourcesModel));
        resourcesTable.add(resourceListing);

        // pagination
        add(new BootstrapAjaxPagingNavigator("paging", resourceListing) {

            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() > 1);
            }

        });

        // panel for records with no resources
        add(createNoResourcesContainer("noResources")
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(resourceListing.getPageCount() == 0);
                    }
                })
        );

        //For Ajax updating of resource listing when paging
        setOutputMarkupId(true);
    }

    /**
     * Special item in the table for the landing page
     *
     * @param id
     * @param documentModel
     * @return
     */
    private ResourceLinksPanelItem createLandingPageItem(final String id) {
        final IModel<String> landingPageModel = new SolrFieldStringModel(getModel(), fieldNameService.getFieldName(FieldKey.LANDINGPAGE));
        final ResourceInfoModel landingPageInfoModel = new ResourceInfoModel(resourceStringConverter, landingPageModel);
        final IModel<Boolean> landingPageDetailsModel = Model.of(Boolean.FALSE);
        final ResourceLinksPanelItem landingPageItem = new ResourceLinksLandingPageItem(id, landingPageInfoModel, getModel(), landingPageDetailsModel) {
            @Override
            protected void onDetailsToggleClick(String id, Optional<AjaxRequestTarget> target) {
                landingPageDetailsModel.setObject(!landingPageDetailsModel.getObject());

                target.ifPresent(t -> {
                    t.add(resourcesTable);
                });
            }

        };
        landingPageItem.add(new InvisibleIfNullBehaviour(landingPageModel));
        return landingPageItem;
    }

    /**
     * Creates a container to show in case of no resource links
     *
     * @param id
     * @return
     */
    private MarkupContainer createNoResourcesContainer(String id) {
        final MarkupContainer container = new WebMarkupContainer(id);

        //landing page link
        final IModel<String> landingPageModel = new PIDLinkModel( // wrap in model that transforms handle links
                new PropertyModel(new ResourceInfoObjectModel(getModel(), fieldNameService.getFieldName(FieldKey.LANDINGPAGE)), "url")); // get landing page from document
        container.add(new WebMarkupContainer("landingPageContainer") {
            @Override
            protected void onConfigure() {
                setVisible(landingPageModel.getObject() != null);
            }

        }.add(new ExternalLink("landingPageLink", landingPageModel)));

        //hierarchy link
        final SolrFieldModel<String> partCountModel
                = new SolrFieldModel<>(getModel(), fieldNameService.getFieldName(FieldKey.HAS_PART_COUNT));
        container.add(new WebMarkupContainer("hierarchyLinkContainer") {
            @Override
            protected void onConfigure() {
                setVisible(partCountModel.getObject() != null);
            }
        }.add(new AjaxFallbackLink<Void>("hierarchyLink") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                switchToTab(HIERARCHY_SECTION, target);
            }
        }));

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
            final IModel<Boolean> itemDetailsShownModel = new AbstractReadOnlyModel<Boolean>() {
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

                    target.ifPresent(t->{
                        t.add(resourcesTable);
                    });
                }

            });
        }
    }

    protected abstract void switchToTab(String tab, Optional<AjaxRequestTarget> target);

}
