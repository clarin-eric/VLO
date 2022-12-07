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
import java.util.Optional;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * An special type of item in the links table for a link to the metadata
 * hierarchy
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see ResourceLinksPanel
 */
public abstract class ResourceLinksPanelHierarchyItem extends GenericPanel<Integer> {

    public ResourceLinksPanelHierarchyItem(String id, IModel<Integer> childrenCountModel) {
        super(id, childrenCountModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        // Resource type icon
        add(new ResourceTypeIcon("resourceTypeIcon", Model.of(ResourceTypeIcon.HIERARCHY)));
        add(new AjaxFallbackLink<String>("hierarchyTabLink") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> target) {
                switchToHierarchyTab(target);
            }
        }.add(new Label("label", new StringResourceModel("hierarchyLinkLabel", getModel()))));
    }

    protected abstract void switchToHierarchyTab(Optional<AjaxRequestTarget> target);

}
