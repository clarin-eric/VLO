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
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.LazyResourceInfoUpdateBehavior;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeGlyphicon;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that shows all resources represented by a collection of resource
 * strings as links that trigger a details panel for the selected resource
 *
 * @author twagoo
 */
public class ResourceLinksPanel extends Panel {

    private static final int ITEMS_PER_PAGE = 12;

    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;
    @SpringBean(name = "resolvingResourceStringConverter")
    private ResourceStringConverter resolvingResourceStringConverter;

    private final IModel<Boolean> detailsVisibleModel = new Model<>(Boolean.FALSE);

    /**
     *
     * @param id panel id
     * @param documentModel model of document that holds the resources
     */
    public ResourceLinksPanel(String id, IModel<SolrDocument> documentModel) {
        super(id, documentModel);

        final SolrFieldModel<String> resourcesModel
                = new SolrFieldModel<>(documentModel, FacetConstants.FIELD_RESOURCE);
        final IModel<String> landingPageModel
                // wrap in model that transforms handle links
                = new HandleLinkModel(
                        // get landing page from document
                        new SolrFieldStringModel(documentModel, FacetConstants.FIELD_LANDINGPAGE));

        // create table of resources with optional details
        final ResourcesListView resourceListing = new ResourcesListView("resource", new CollectionListModel<>(resourcesModel));
        add(createResourcesTable("resources", resourceListing));

        // pagination
        add(new BootstrapAjaxPagingNavigator("paging", resourceListing) {

            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() > 1);
            }

        });

        add(new MarkupContainer("noResources") {

            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() == 0);
            }

        }.add(new WebMarkupContainer("landingPageContainer") {
            @Override
            protected void onConfigure() {
                setVisible(landingPageModel.getObject() != null);
            }

        }.add(new ExternalLink("landingPageLink", landingPageModel))));

        //For Ajax updating of resource listing when paging
        setOutputMarkupId(true);
    }

    private WebMarkupContainer createResourcesTable(String id, final ResourcesListView resourceListing) {
        final WebMarkupContainer resourceListContainer = new WebMarkupContainer(id) {
            @Override
            protected void onConfigure() {
                setVisible(resourceListing.getPageCount() > 0);
            }
            
        };
        //add the actual listing
        resourceListContainer.add(resourceListing);
        
        // headers for details columns
        resourceListContainer.add(
                new WebMarkupContainer("detailsHeaderColumns")
                        .add(new Link("toggleDetails") {
                            @Override
                            public void onClick() {
                                //toggle visibility
                                detailsVisibleModel.setObject(!detailsVisibleModel.getObject());
                            }
                        })
                        .add(BooleanVisibilityBehavior.visibleOnTrue(detailsVisibleModel)) //only show when expanded
        );
        // header with detail expansion link
        resourceListContainer.add(
                new WebMarkupContainer("expandHeaderColumn")
                        .add(new Link("toggleDetails") {
                            @Override
                            public void onClick() {
                                //toggle visibility
                                detailsVisibleModel.setObject(!detailsVisibleModel.getObject());
                            }
                        })
                        .add(BooleanVisibilityBehavior.visibleOnFalse(detailsVisibleModel)) //only show when not expanded
        );
        
        resourceListContainer.setOutputMarkupId(true);
        return resourceListContainer;
    }

    private class ResourcesListView extends PageableListView<String> {

        public ResourcesListView(String id, IModel<? extends List<String>> model) {
            super(id, model, ITEMS_PER_PAGE);
            setReuseItems(true);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(resourceStringConverter, item.getModel());
            item.setDefaultModel(new CompoundPropertyModel<>(resourceInfoModel));

            // add a link that will show the resource details panel when clicked
            // wrap href in model that transforms handle links
            final IModel<String> linkModel = new HandleLinkModel(new PropertyModel(resourceInfoModel, "href"));
            final ExternalLink link = new ExternalLink("showResource", linkModel);

            item.add(new ResourceTypeGlyphicon("resourceTypeIcon", new PropertyModel(resourceInfoModel, "resourceType")));;

            // set the file name as the link's text content
            link.add(new Label("fileName", new PropertyModel(resourceInfoModel, "fileName")));
            // make the link update via AJAX with resolved location (in case of handle)
            link.add(new LazyResourceInfoUpdateBehavior(resolvingResourceStringConverter, resourceInfoModel) {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(link);
                }
            });

            link.setOutputMarkupId(true);
            item.add(link);

            // get the friendly name of the resource type dynamically from the resource bundle
            item.add(new Label("resourceType", StringResourceModelMigration.of("resourcetype.${resourceType}.singular", resourceInfoModel, resourceInfoModel.getObject().getResourceType())));

            //detailed properties
            item.add(new WebMarkupContainer("detailsColumns")
                    .add(new Label("mimeType"))
                    .add(new Label("href"))
                    .add(BooleanVisibilityBehavior.visibleOnTrue(detailsVisibleModel))
            );
            item.add(new WebMarkupContainer("expandColumn")
                    .add(BooleanVisibilityBehavior.visibleOnFalse(detailsVisibleModel))
            );

        }
    }

    private static class BooleanVisibilityBehavior extends Behavior {

        private final IModel<Boolean> model;
        private final Boolean visibilityState;

        private BooleanVisibilityBehavior(IModel<Boolean> model, Boolean visibilityState) {
            this.model = model;
            this.visibilityState = visibilityState;
        }

        @Override
        public void onConfigure(Component component) {
            component.setVisible(visibilityState.equals(model.getObject()));
        }

        public static BooleanVisibilityBehavior visibleOnTrue(IModel<Boolean> model) {
            return new BooleanVisibilityBehavior(model, Boolean.TRUE);
        }

        public static BooleanVisibilityBehavior visibleOnFalse(IModel<Boolean> model) {
            return new BooleanVisibilityBehavior(model, Boolean.FALSE);
        }

    }

}
