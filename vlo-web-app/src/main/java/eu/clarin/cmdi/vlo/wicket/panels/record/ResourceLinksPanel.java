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

import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import java.util.Collection;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that shows all resources represented by a collection of resource
 * strings as links that trigger a details panel for the selected resource
 *
 * @author twagoo
 */
public class ResourceLinksPanel extends Panel {

    @SpringBean
    private ResourceStringConverter resourceStringConverter;
    private final WebMarkupContainer detailsContainer;

    /**
     *
     * @param id panel id
     * @param model model that holds the collection of resource strings
     */
    public ResourceLinksPanel(String id, IModel<Collection<String>> model) {
        super(id, model);
        // list view that shows all resources as links that show a resource details panel when clicked
        add(new ResourcesListView("resource", new CollectionListModel<String>(model)));

        // container for resource details (to enable AJAX updates)
        detailsContainer = new WebMarkupContainer("detailsContainer");
        detailsContainer.setOutputMarkupId(true);
        add(detailsContainer);

        // insert a place holder until one of the resource links is clicked
        final WebMarkupContainer detailsPlaceholder = new WebMarkupContainer("details");
        detailsPlaceholder.setVisible(false);
        detailsContainer.add(detailsPlaceholder);
    }

    private class ResourcesListView extends ListView<String> {

        public ResourcesListView(String id, IModel<? extends List<? extends String>> model) {
            super(id, model);
            setReuseItems(true);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(item.getModel());
            // add a link that will show the resource details panel when clicked
            item.add(createLink(resourceInfoModel));
        }

        private Link createLink(final ResourceInfoModel resourceInfoModel) {
            final Link link = new AjaxFallbackLink("showResource") {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    // replace any existing details panel or placeholder with
                    // a details panel for the current resource
                    detailsContainer.addOrReplace(new ResourceLinkDetailsPanel("details", resourceInfoModel));
                    if (target != null) {
                        target.add(detailsContainer);
                        target.prependJavaScript("hideResourceDetails();");
                        target.appendJavaScript("showResourceDetails();");
                    }
                }
            };
            link.setAnchor(detailsContainer);
            // set the file name as the link's text content
            link.add(new Label("filename", new PropertyModel(resourceInfoModel, "fileName")));

            // set the class attribute on the link from the value associated
            // with the resource type as defined in the properties file
            final StringResourceModel linkClass = new StringResourceModel("class.${resourceType}", ResourceLinksPanel.this, resourceInfoModel, "");
            link.add(new AttributeAppender("class", linkClass).setSeparator(" "));

            return link;
        }
    }

    /**
     * Model for {@link ResourceInfo} that dynamically instantiates its objects
     * from a resource string (as retrieved from the Solr index) using the
     * {@link ResourceStringConverter}
     */
    private class ResourceInfoModel extends LoadableDetachableModel<ResourceInfo> {

        private final IModel<String> resourceStringModel;

        public ResourceInfoModel(IModel<String> resourceStringModel) {
            this.resourceStringModel = resourceStringModel;
        }

        @Override
        protected ResourceInfo load() {
            return resourceStringConverter.getResourceInfo(resourceStringModel.getObject());
        }

        @Override
        public void detach() {
            super.detach();
            resourceStringModel.detach();
        }

    }

}
