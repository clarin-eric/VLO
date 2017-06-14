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

import com.google.common.collect.Ordering;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import java.util.Collection;
import java.util.Iterator;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

/**
 * Model that provides field values as String values for a given field values
 * model, both for singular values and multiple values (imploding the latter
 * into a single string)
 *
 * @author twagoo
 */
public class SolrFieldStringModel extends AbstractReadOnlyModel<String> {

    private final IModel<Collection<Object>> fieldModel;
    private final String field;
    private final boolean forceSingleValue;

    /**
     * Wraps the document model and specified field name into a
     * {@link SolrFieldModel} to obtain field values. Single value will not be
     * forced.
     *
     * @param documentModel model of document that holds the field values
     * @param fieldName name of the field to take value from
     */
    public SolrFieldStringModel(IModel<SolrDocument> documentModel, String fieldName) {
        this(documentModel, fieldName, false);
    }

    /**
     * Wraps the document model and specified field name into a
     * {@link SolrFieldModel} to obtain field values
     *
     * @param documentModel model of document that holds the field values
     * @param fieldName name of the field to take value from
     * @param forceSingleValue if set to true, only the first encountered value will be considered
     */
    public SolrFieldStringModel(IModel<SolrDocument> documentModel, String fieldName, boolean forceSingleValue) {
        fieldModel = new SolrFieldModel<>(documentModel, fieldName);
        field = fieldName;
        this.forceSingleValue = forceSingleValue;
    }

    @Override
    public String getObject() {
        final Collection<Object> fieldValues = fieldModel.getObject();
        if (fieldValues != null) {
            return getValueString(fieldValues);
        }
        return null;
    }

    private String getValueString(final Collection<Object> fieldValues) {
        // apply ordering if available and applicable
        final Ordering ordering = getFieldValueOrdering();
        final Iterator<Object> iterator;
        if (ordering == null || fieldValues.size() <= 1) {
            iterator = fieldValues.iterator();
        } else {
            iterator = ordering.immutableSortedCopy(fieldValues).iterator();
        }

        if (iterator.hasNext()) {
            final String firstValue = iterator.next().toString();
            if (iterator.hasNext() && !forceSingleValue) {
                return getMultipleValuesString(firstValue, iterator);
            } else {
                return postprocessValue(firstValue);
            }
        } else {
            return null;
        }
    }

    protected String getMultipleValuesString(final String firstValue, final Iterator<Object> iterator) {
        // for multiple value strings, run every individual value through the converter
        final StringBuilder valuesBuilder = new StringBuilder(postprocessValue(firstValue)).append("; ");
        while (iterator.hasNext()) {
            valuesBuilder.append(postprocessValue(iterator.next().toString()));
            if (iterator.hasNext()) {
                valuesBuilder.append("; ");
            }
        }
        return valuesBuilder.toString();
    }

    @Override
    public void detach() {
        super.detach();
        fieldModel.detach();
    }

    private String postprocessValue(String value) {
        if (value != null) {
            final IConverter<String> converter = getFieldValueConverter();
            if (converter != null) {
                return converter.convertToString(value, null);
            }
        }
        return value;
    }

    private IConverter<String> getFieldValueConverter() {
        return VloWicketApplication.get().getFieldValueConverterProvider().getConverter(field);
    }

    private Ordering getFieldValueOrdering() {
        return VloWicketApplication.get().getFieldValueOrderings().get(field);
    }

}
