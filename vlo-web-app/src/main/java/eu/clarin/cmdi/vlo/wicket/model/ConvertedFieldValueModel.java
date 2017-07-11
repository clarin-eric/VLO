/*
 * Copyright (C) 2016 CLARIN
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

import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import org.apache.wicket.Session;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only model that wraps a string model that provides a field value and a
 * field name, for which it returns the converted value if available via the
 * {@link FieldValueConverterProvider} registered on
 * {@link VloWicketApplication}
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class ConvertedFieldValueModel extends AbstractReadOnlyModel<String> {
    
    private final static Logger logger = LoggerFactory.getLogger(ConvertedFieldValueModel.class);
    
    private final IModel<String> valueModel;
    private final String field;
    
    public ConvertedFieldValueModel(IModel<String> valueModel, String field) {
        this.valueModel = valueModel;
        this.field = field;
    }
    
    @Override
    public String getObject() {
        final IConverter<String> converter = VloWicketApplication.get().getFieldValueConverterProvider().getConverter(field);
        if (converter == null) {
            logger.warn("No converter for field {}", field);
            return valueModel.getObject();
        } else {
            return converter.convertToString(valueModel.getObject(), Session.get().getLocale());
        }
    }
    
    @Override
    public void detach() {
        valueModel.detach();
    }
    
}
