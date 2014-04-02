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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.pojo.SearchContext;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class SearchResultItemPanel extends Panel {

    private final SearchResultItemCollapsedPanel collapsedDetails;

    public SearchResultItemPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel) {
        super(id, documentModel);

        final Link recordLink = new RecordPageLink("recordLink", documentModel, selectionModel);
        recordLink.add(new SolrFieldLabel("title", documentModel, FacetConstants.FIELD_NAME));
        add(recordLink);

        //TODO: Add expand/collapse toggle
        collapsedDetails = new SearchResultItemCollapsedPanel("collapsedDetials", documentModel, selectionModel);
        add(collapsedDetails);

        //TODO: Add expanded details panel
    }
}
