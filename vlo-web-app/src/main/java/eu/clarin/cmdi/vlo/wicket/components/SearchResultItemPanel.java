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

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class SearchResultItemPanel extends Panel {

    public SearchResultItemPanel(String id, IModel<SolrDocument> model) {
        super(id, model);
        add(new SolrFieldLabel("title", model, FacetConstants.FIELD_NAME));
        add(new SolrFieldLabel("description", model, FacetConstants.FIELD_DESCRIPTION, "<no description>"));
    }


    public static class SolrFieldLabel extends Label {

        public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName) {
            super(id, new SolrFieldStringModel(documentModel, fieldName));
        }

        public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback) {
            super(id,
                    new NullFallbackModel(
                            new SolrFieldStringModel(documentModel, fieldName), nullFallback));
        }

    }
}
