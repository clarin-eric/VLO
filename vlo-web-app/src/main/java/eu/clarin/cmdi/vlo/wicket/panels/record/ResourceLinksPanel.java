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

import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.LazyResourceInfoUpdateBehavior;
import eu.clarin.cmdi.vlo.wicket.ResourceTypeCssBehaviour;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import java.util.Collection;
import java.util.List;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
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

    /**
     *
     * @param id panel id
     * @param model model that holds the collection of resource strings
     */
    public ResourceLinksPanel(String id, IModel<Collection<String>> model) {
        super(id, model);

        // list view that shows all resources as links that show a resource details panel when clicked
        final ResourcesListView resourceListing = new ResourcesListView("resource", new CollectionListModel<>(model));
        add(resourceListing);

        // pagination
        add(new AjaxPagingNavigator("paging", resourceListing) {

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

        });
        
        //For Ajax updating of resource listing when paging
        setOutputMarkupId(true);
    }

    private class ResourcesListView extends PageableListView<String> {

        public ResourcesListView(String id, IModel<? extends List<String>> model) {
            super(id, model, ITEMS_PER_PAGE);
            setReuseItems(true);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(resourceStringConverter, item.getModel());

            // add a link that will show the resource details panel when clicked
            // wrap href in model that transforms handle links
            final IModel<String> linkModel = new HandleLinkModel(new PropertyModel(resourceInfoModel, "href"));
            final ExternalLink link = new ExternalLink("showResource", linkModel);

            // set the file name as the link's text content
            link.add(new Label("filename", new PropertyModel(resourceInfoModel, "fileName")));
            // add details panel shown on hover
            link.add(new ResourceLinkDetailsPanel("details", resourceInfoModel));

            // apply the css class matching the resource type
            link.add(new ResourceTypeCssBehaviour(resourceInfoModel));

            // make the link update via AJAX with resolved location (in case of handle)
            link.add(new LazyResourceInfoUpdateBehavior(resolvingResourceStringConverter, resourceInfoModel) {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(link);
                }
            });

            link.setOutputMarkupId(true);
            item.add(link);
        }
    }

}
