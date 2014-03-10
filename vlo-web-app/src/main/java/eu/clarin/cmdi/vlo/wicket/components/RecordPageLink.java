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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class RecordPageLink extends Link {

    private final IModel<SolrDocument> documentModel;
    private final IModel<QueryFacetsSelection> selectionModel;

    public RecordPageLink(String id, IModel<SolrDocument> documentModel, IModel<QueryFacetsSelection> selectionModel) {
        super(id);
        this.documentModel = documentModel;
        this.selectionModel = selectionModel;
    }

    @Override
    public void onClick() {
        setResponsePage(new RecordPage(documentModel, selectionModel));
    }

}
