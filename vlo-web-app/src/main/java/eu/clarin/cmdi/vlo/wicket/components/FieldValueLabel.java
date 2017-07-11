/*
 * Copyright (C) 2015 CLARIN
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

import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProviderImpl;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;

/**
 * Label to be used for facet fields. It will apply the appropriate converter
 * from {@link FieldValueConverterProviderImpl} based on the field name.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FieldValueLabel extends Label {

    @SpringBean
    private FieldValueConverterProvider fieldValueConverters;

    private final IModel<String> fieldModel;

    /**
     * Use some Wicket magic and obtain the value from the property of the
     * parent's default model
     *
     * @param id component id
     * @param fieldModel model that provides the name of the field
     * @see Label#Label(java.lang.String, org.apache.wicket.model.IModel)
     */
    public FieldValueLabel(String id, IModel<String> fieldModel) {
        super(id);
        this.fieldModel = fieldModel;
    }

    /**
     *
     * @param id component id
     * @param model model that provides the field value for this label (which
     * may get converted on render)
     * @param fieldModel model that provides the name of the field
     */
    public FieldValueLabel(String id, IModel<?> model, IModel<String> fieldModel) {
        super(id, model);
        this.fieldModel = fieldModel;
    }

    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type == String.class) {
            final IConverter<String> converter = fieldValueConverters.getConverter(fieldModel.getObject());
            if (converter != null) {
                return (IConverter<C>) converter;
            }
        }
        return super.getConverter(type);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        fieldModel.detach();
    }

}
