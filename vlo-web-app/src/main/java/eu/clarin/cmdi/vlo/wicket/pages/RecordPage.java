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
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class RecordPage extends WebPage {

    private final IModel<SolrDocument> documentModel;
    private final IModel<QueryFacetsSelection> contextModel;

    public RecordPage(IModel<SolrDocument> documentModel, IModel<QueryFacetsSelection> contextModel) {
        super(documentModel);
        this.documentModel = documentModel;
        this.contextModel = contextModel;
    }

    @Override
    public void detachModels() {
        super.detachModels();
        // not passed to parent
        contextModel.detach();
    }

}
