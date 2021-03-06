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
import eu.clarin.cmdi.vlo.wicket.panels.ContentSearchFormPanel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * An special type of item in the links table for a content search service link
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see ResourceLinksPanel
 */
public class ResourceLinksPanelSearchServiceItem extends GenericPanel<String> {

    private final IModel<SolrDocument> documentModel;

    public ResourceLinksPanelSearchServiceItem(String id, IModel<String> endpointModel, IModel<SolrDocument> documentModel) {
        super(id, endpointModel);
        this.documentModel = documentModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        // Resource type icon
        add(createResourceTypeIcon("resourceTypeIcon"));
        // Content search form
        add(new ContentSearchFormPanel("contentSearchForm", documentModel, getModel()));
    }

    protected ResourceTypeIcon createResourceTypeIcon(String id) {
        return new ResourceTypeIcon(id, Model.of(ResourceTypeIcon.SEARCH_SERVICE));
    }

    @Override
    public void detachModels() {
        super.detachModels();
        documentModel.detach();
    }

}
