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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.pojo.ResourceInfo;
import eu.clarin.cmdi.vlo.service.ResourceStringConverter;
import eu.clarin.cmdi.vlo.wicket.model.CollectionListModel;
import java.util.Collection;
import org.apache.wicket.AttributeModifier;
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
 *
 * @author twagoo
 */
public class ResourceLinksPanel extends Panel {

    @SpringBean
    private ResourceStringConverter resourceStringConverter;

    /**
     *
     * @param id panel id
     * @param model model that holds the collection of resource strings
     */
    public ResourceLinksPanel(String id, IModel<Collection<String>> model) {
        super(id, model);
        final ListView<String> resourcesView = new ListView<String>("resource", new CollectionListModel<String>(model)) {

            @Override
            protected void populateItem(ListItem<String> item) {
                final ResourceInfoModel resourceInfoModel = new ResourceInfoModel(item.getModel());
                final Link link = new Link("showResource") {

                    @Override
                    public void onClick() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
                item.add(link);
                // set the file name as the link's text content
                link.add(new Label("filename", new PropertyModel(resourceInfoModel, "href")));
                // set the class attribute on the link from the value associated
                // with the resource type as defined in the properties file
                link.add(new AttributeModifier("class",
                        new StringResourceModel("class.${resourceType}", ResourceLinksPanel.this, resourceInfoModel, "")));
            }
        };
        resourcesView.setReuseItems(true);
        add(resourcesView);

    }

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
