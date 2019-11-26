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

import eu.clarin.cmdi.vlo.wicket.BooleanVisibilityBehavior;
import eu.clarin.cmdi.vlo.wicket.components.ResourceTypeIcon;
import eu.clarin.cmdi.vlo.wicket.model.ResourceInfoModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResourceLinksSpecialItem extends ResourceLinksPanelItem {

    private final IModel<String> resourceTypeIconModel;
    private final IModel<String> resourceTypeLabelModel;

    public ResourceLinksSpecialItem(String id, IModel<String> resourceTypeLabel, IModel<String> resourceTypeIcon, ResourceInfoModel resourceInfoModel, IModel<SolrDocument> documentModel, IModel<Boolean> detailsVisibleModel) {
        super(id, resourceInfoModel, documentModel, detailsVisibleModel);
        this.resourceTypeLabelModel = resourceTypeLabel;
        this.resourceTypeIconModel = resourceTypeIcon;
    }

    @Override
    protected ResourceTypeIcon createResourceTypeIcon(String id) {
        // always show landing page icon ('home' icon)
        return new ResourceTypeIcon(id, resourceTypeIconModel);
    }

    @Override
    protected Label createResourceTypeLabel(String id) {
        // resource type: ignore mime type, always show as landing page
        return new Label(id, resourceTypeLabelModel);
    }

    @Override
    protected Component createOptionsDropdown(IModel<String> linkModel, ResourceInfoModel resourceInfoModel) {
        return super.createOptionsDropdown(linkModel, resourceInfoModel)
                .add(BooleanVisibilityBehavior.visibleOnTrue(this::isOptionsDropdownEnabled));
    }

    protected boolean isOptionsDropdownEnabled() {
        return false;
    }

}
