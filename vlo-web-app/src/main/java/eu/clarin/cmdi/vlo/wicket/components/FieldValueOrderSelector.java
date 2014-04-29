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

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import eu.clarin.cmdi.vlo.wicket.panels.search.AllFacetValuesPanel;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * A drop down choice component that offers a selection of order modes for facet
 * field values.
 *
 * Designed for usage in {@link AllFacetValuesPanel}
 *
 * @author twagoo
 */
public class FieldValueOrderSelector extends DropDownChoice<SortParam<FieldValuesOrder>> {

    public final static SortParam<FieldValuesOrder> COUNT_SORT = new SortParam<FieldValuesOrder>(FieldValuesOrder.COUNT, false);
    public final static SortParam<FieldValuesOrder> NAME_SORT = new SortParam<FieldValuesOrder>(FieldValuesOrder.NAME, true);
    public final static List<SortParam<FieldValuesOrder>> SORT_CHOICES = ImmutableList.of(COUNT_SORT, NAME_SORT);
    public static final String COUNT_KEY = "fields.order.count";
    public static final String NAME_KEY = "fields.order.name";

    public FieldValueOrderSelector(String id, IModel<SortParam<FieldValuesOrder>> model) {
        super(id, model, SORT_CHOICES);
    }

    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type == SortParam.class) {
            // This should always be the case
            return (IConverter<C>) new FieldValueSortParamConverter();
        } else {
            return super.getConverter(type);
        }
    }

    /**
     * Converts between sort parameter objects and string representations
     * thereof (string values coming from a properties resource bundle)
     */
    private class FieldValueSortParamConverter implements IConverter<SortParam<FieldValuesOrder>> {

        @Override
        public SortParam<FieldValuesOrder> convertToObject(String value, Locale locale) throws ConversionException {
            if (value.equals(getString(NAME_KEY))) {
                return NAME_SORT;
            } else if (value.equals(getString(COUNT_KEY))) {
                return COUNT_SORT;
            } else {
                throw new ConversionException("Unknown value " + value);
            }
        }

        @Override
        public String convertToString(SortParam<FieldValuesOrder> value, Locale locale) {
            switch (value.getProperty()) {
                case NAME:
                    return getString(NAME_KEY);
                case COUNT:
                    return getString(COUNT_KEY);
                default:
                    return "???";
            }
        }
    }

}
