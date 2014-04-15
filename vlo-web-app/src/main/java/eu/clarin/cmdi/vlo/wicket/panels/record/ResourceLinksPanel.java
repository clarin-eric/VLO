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
import eu.clarin.cmdi.vlo.wicket.ResourceTypeCssBehaviour;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import eu.clarin.cmdi.vlo.wicket.model.HandleLinkModel;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import java.util.Collection;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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

    @SpringBean(name = "resourceStringConverter")
    private ResourceStringConverter resourceStringConverter;

    /**
     *
     * @param id panel id
     * @param model model that holds the collection of resource strings
     */
    public ResourceLinksPanel(String id, IModel<Collection<String>> model) {
        super(id, model);
        // list view that shows all resources as links that show a resource details panel when clicked
        add(new ResourcesListView("resource", new CollectionListModel<String>(model)));
    }

    private class ResourcesListView extends ListView<String> {

        public ResourcesListView(String id, IModel<? extends List<? extends String>> model) {
            super(id, model);
            setReuseItems(true);
        }

        @Override
        protected void populateItem(ListItem<String> item) {
            final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(resourceStringConverter, item.getModel());
            // add a link that will show the resource details panel when clicked
            item.add(createLink(resourceInfoModel));
        }

        private Component createLink(final ResourceInfoModel resourceInfoModel) {
            // wrap href in model that transforms handle links
            final IModel<String> linkModel = new HandleLinkModel(new PropertyModel(resourceInfoModel, "href"));
            final ExternalLink link = new ExternalLink("showResource", linkModel);

            // set the file name as the link's text content
            link.add(new Label("filename", new PropertyModel(resourceInfoModel, "fileName")));
            // add details panel shown on hover
            link.add(new ResourceLinkDetailsPanel("details", resourceInfoModel));

            // apply the css class matching the resource type
            link.add(new ResourceTypeCssBehaviour(resourceInfoModel));

            return link;
        }
    }

}
