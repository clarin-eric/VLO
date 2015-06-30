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
package eu.clarin.cmdi.vlo.wicket.model;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import java.util.Collection;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Model that provides field values as for a given Solr document
 *
 * @author twagoo
 * @param <T> type of elements in the value collection in the field. Values will
 * be cast on the fly, so specifying a non-matching type here might result in
 * runtime errors!
 */
public class SolrFieldModel<T> extends AbstractReadOnlyModel<Collection<T>> {

    private final IModel<SolrDocument> documentModel;
    private final IModel<String> fieldNameModel;

    /**
     *
     * @param documentModel model of document that holds the field values
     * @param fieldName name of the field to take value from
     */
    public SolrFieldModel(IModel<SolrDocument> documentModel, String fieldName) {
        this(documentModel, Model.of(fieldName));
    }

    /**
     *
     * @param documentModel model of document that holds the field values
     * @param fieldNameModel model that provides the field name
     */
    public SolrFieldModel(IModel<SolrDocument> documentModel, IModel<String> fieldNameModel) {
        this.documentModel = documentModel;
        this.fieldNameModel = fieldNameModel;
    }

    @Override
    public Collection<T> getObject() {
        final SolrDocument document = documentModel.getObject();
        if (document != null) {
            final Collection<Object> fieldValues = document.getFieldValues(fieldNameModel.getObject());
            if (fieldValues != null) {
                return transformCollectionType(fieldValues);
            }
        }
        return null;
    }

    /**
     * Transforms object collection to a typed collection by means of an
     * on-the-fly cast
     *
     * @param fieldValues
     * @return
     */
    private Collection<T> transformCollectionType(final Collection<Object> fieldValues) {
        return Collections2.transform(fieldValues, new Function<Object, T>() {

            @Override
            public T apply(Object input) {
                return (T) input;
            }
        });
    }

    @Override
    public void detach() {
        super.detach();
        documentModel.detach();
    }

}
