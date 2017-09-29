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

import eu.clarin.cmdi.vlo.wicket.model.NullFallbackModel;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldStringModel;
import eu.clarin.cmdi.vlo.wicket.model.TruncatingStringModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * Label that shows the content of a Solr field by its string value (using
 * {@link SolrFieldStringModel})
 */
public class SolrFieldLabel extends Label {

    /**
     *
     * @param id id of label
     * @param documentModel model that holds document to show field of
     * @param fieldName name of field to show value of
     * @param nullFallback string to show if actual value is null
     */
    public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback) {
        this(id, documentModel, fieldName, nullFallback, false);
    }

    public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, IModel<String> nullFallback) {
        this(id, documentModel, fieldName, nullFallback, false);
    }

    protected SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, IModel<String> nullFallback, boolean forceSingleValue) {
        super(id, new NullFallbackModel(new SolrFieldStringModel(documentModel, fieldName, forceSingleValue), nullFallback));
    }

    protected SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback, boolean forceSingleValue) {
        super(id, new NullFallbackModel(new SolrFieldStringModel(documentModel, fieldName, forceSingleValue), nullFallback));
    }

    /**
     *
     * @param id id of label
     * @param documentModel model that holds document to show field of
     * @param fieldName name of field to show value of
     * @param nullFallback string to show if actual value is null
     * @param maxLength maximum length to allow
     * @param truncatePoint point to truncate if string is too long
     */
    public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback, int maxLength, int truncatePoint) {
        this(id, documentModel, fieldName, nullFallback, maxLength, truncatePoint, false);
    }

    public SolrFieldLabel(String id, IModel<SolrDocument> documentModel, String fieldName, String nullFallback, int maxLength, int truncatePoint, boolean forceSingleValue) {
        super(id, new NullFallbackModel(
                new TruncatingStringModel(
                        new SolrFieldStringModel(documentModel, fieldName, forceSingleValue), maxLength, truncatePoint), nullFallback));
    }

}
