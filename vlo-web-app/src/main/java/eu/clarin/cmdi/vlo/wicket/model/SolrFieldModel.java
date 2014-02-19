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

import java.util.Collection;
import java.util.Iterator;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * Model that provides field values as String values for a given Solr document
 * and a field name, both for singular values and multiple values (imploding the
 * latter into a single string)
 *
 * @author twagoo
 */
public class SolrFieldModel extends AbstractReadOnlyModel<String> {

    private final IModel<SolrDocument> documentModel;
    private final String fieldName;

    /**
     * 
     * @param documentModel model of document that holds the field values
     * @param fieldName name of the field to take value from
     */
    public SolrFieldModel(IModel<SolrDocument> documentModel, String fieldName) {
        this.documentModel = documentModel;
        this.fieldName = fieldName;
    }

    @Override
    public String getObject() {
        final Collection<Object> fieldValues = documentModel.getObject().getFieldValues(fieldName);
        if (fieldValues != null) {
            return getValueString(fieldValues);
        }
        return null;
    }

    private String getValueString(final Collection<Object> fieldValues) {
        final Iterator<Object> iterator = fieldValues.iterator();
        if (iterator.hasNext()) {
            final String firstValue = iterator.next().toString();
            if (iterator.hasNext()) {
                return getMultipleValuesString(firstValue, iterator);
            } else {
                return firstValue;
            }
        } else {
            return null;
        }
    }

    protected String getMultipleValuesString(final String firstValue, final Iterator<Object> iterator) {
        final StringBuilder valuesBuilder = new StringBuilder(firstValue);
        while (iterator.hasNext()) {
            valuesBuilder.append(iterator.next().toString());
            if (iterator.hasNext()) {
                valuesBuilder.append("; ");
            }
        }
        return valuesBuilder.toString();
    }
}
