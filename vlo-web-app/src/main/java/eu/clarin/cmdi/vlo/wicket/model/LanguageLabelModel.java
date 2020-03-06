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
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class LanguageLabelModel implements IModel<List<String>>{
    private final IModel<Collection<Object>> fieldModel;
    private final String field;

    public LanguageLabelModel(IModel<SolrDocument> documentModel, String fieldName) {
        fieldModel = new SolrFieldModel<>(documentModel, fieldName);
        field = fieldName;
    }

    @Override
    public List<String> getObject() {
        final Collection<Object> fieldValues = fieldModel.getObject();
        if (fieldValues != null) {
            return getMultipleValuesString(fieldValues);
        }
        return null;
    }


    private List<String> getMultipleValuesString(final Collection<Object> fieldValues) {
        final Iterator<?> iterator = fieldValues.iterator();
        List<String> languages = new ArrayList<String>();
        while (iterator.hasNext()) {
            languages.add(postprocessValue(iterator.next().toString()));
        }
        return languages;
    }

    @Override
    public void detach() {
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
}
