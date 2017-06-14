/*
 * Copyright (C) 2017 CLARIN
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

import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;

/**
 * {@link SolrFieldLabel} extension that forces only a single value (the first
 * value) of the field to be shown.
 *
 * @author twagoo
 * @see
 * SolrFieldStringModel#SolrFieldStringModel(org.apache.wicket.model.IModel,
 * java.lang.String, boolean)
 */
public class SingleValueSolrFieldLabel extends SolrFieldLabel {

    /**
     *
     * @param id id of label
     * @param documentModel model that holds document to show field of
     * @param fieldName name of field to show value of
     * @param nullFallback string to show if actual value is null
     */
    public SingleValueSolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback) {
        super(id, documentModel, fieldName, nullFallback, true);
    }

    public SingleValueSolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, IModel<String> nullFallback) {
        super(id, documentModel, fieldName, nullFallback, true);
    }
}
