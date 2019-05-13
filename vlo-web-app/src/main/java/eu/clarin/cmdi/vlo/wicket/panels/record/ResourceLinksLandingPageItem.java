/*
 * Copyright (C) 2019 CLARIN
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

import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResourceLinksLandingPageItem extends ResourceLinksPanelItem {

    public ResourceLinksLandingPageItem(String id, ResourceInfoModel resourceInfoModel, IModel<SolrDocument> documentModel, IModel<Boolean> detailsVisibleModel) {
        super(id, resourceInfoModel, documentModel, detailsVisibleModel);
    }

    @Override
    protected ResourceTypeIcon createResourceTypeIcon(String id) {
        // always show landing page icon ('home' icon)
        return new ResourceTypeIcon(id, Model.of(ResourceTypeIcon.LANDING_PAGE));
    }

    @Override
    protected Label createResourceTypeLabel(String id) {
        // resource type: ignore mime type, always show as landing page
        return new Label(id, "landing page");
    }

    @Override
    protected Component createOptionsDropdown(IModel<String> linkModel, ResourceInfoModel resourceInfoModel) {
        return super.createOptionsDropdown(linkModel, resourceInfoModel)
                // hide options menu
                .add(new Behavior() {
                    @Override
                    public void onConfigure(Component component) {
                        component.setVisible(false);
                    }

                });
    }

}
