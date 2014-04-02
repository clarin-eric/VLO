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
import eu.clarin.cmdi.vlo.service.FieldFilter;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.components.SolrFieldLabel;
import eu.clarin.cmdi.vlo.wicket.panels.record.FieldsTablePanel;
import eu.clarin.cmdi.vlo.wicket.provider.DocumentFieldsProvider;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class SearchResultItemExpandedPanel extends GenericPanel<SolrDocument> {

    @SpringBean(name = "basicPropertiesFilter") //TODO: make separate filter for properties in search result items
    private FieldFilter basicPropertiesFilter;
    private final IModel<SearchContext> selectionModel;

    public SearchResultItemExpandedPanel(String id, IModel<SolrDocument> documentModel, IModel<SearchContext> selectionModel) {
        super(id, documentModel);
        this.selectionModel = selectionModel;

        // add untruncated description
        add(new SolrFieldLabel("description", documentModel, FacetConstants.FIELD_DESCRIPTION, "<no description>"));
        add(new RecordPageLink("recordLink", documentModel, selectionModel));

        add(new FieldsTablePanel("documentProperties", new DocumentFieldsProvider(documentModel, basicPropertiesFilter)));
        
        //TODO: Add resource list
    }

    @Override
    public void detachModels() {
        super.detachModels();
        // not passed to super
        selectionModel.detach();
    }

}
