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
package eu.clarin.cmdi.vlo.wicket.provider;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.FieldValueDescriptor;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FieldValueConverterProviderImpl implements FieldValueConverterProvider {

    private final static Pattern LANGUAGE_CODE_PATTERN = Pattern.compile(FacetConstants.LANGUAGE_CODE_PATTERN);
    private final LanguageCodeUtils languageCodeUtils;
    private final FieldValueConverter availabilityConverter;

    public FieldValueConverterProviderImpl(LanguageCodeUtils languageCodeUtils, VloConfig vloConfig) {
        this.languageCodeUtils = languageCodeUtils;
        this.availabilityConverter = new FieldDescriptiorValueConverter(FieldValueDescriptor.toMap(vloConfig.getAvailabilityValues()));
    }

    @Override
    public IConverter<String> getConverter(String fieldName) {
        switch (fieldName) {
            case FacetConstants.FIELD_LANGUAGE_CODE:
                return languageCodeConverter;
            case FacetConstants.FIELD_DESCRIPTION:
                return descriptionConverter;
            case FacetConstants.FIELD_AVAILABILITY:
                return availabilityConverter;
            default:
                return null;
        }

    }

    /**
     * Abstract base class for one directional string converters
     */
    private abstract class FieldValueConverter implements IConverter<String> {

        @Override
        public String convertToObject(String value, Locale locale) throws ConversionException {
            //inversion not supported
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String convertToString(String value, Locale locale) {
            final String converted = getConvertedValue(value, locale);
            if (converted == null) {
                return value;
            } else {
                return converted;
            }
        }

        /**
         * Gets a converted value for the provided value and locale for {@link #convertToString(java.lang.String, java.util.Locale)
         * }. Can return null in which case the original value needs to be
         * provided by the calling method.
         *
         * @param value value to convert
         * @param locale locale for conversion
         * @return converted value or null if no converted value is available
         */
        protected abstract String getConvertedValue(String value, Locale locale);

    }

    /**
     * Converter for language code/name strings (following the pattern of
     * {@link #LANGUAGE_CODE_PATTERN}) into language names
     *
     * @see LanguageCodeUtils#getLanguageNameForLanguageCode(java.lang.String)
     */
    private final FieldValueConverter languageCodeConverter = new FieldValueConverter() {

        @Override
        public String getConvertedValue(String fieldValue, Locale locale) throws ConversionException {
            final Matcher matcher = LANGUAGE_CODE_PATTERN.matcher(fieldValue);
            if (matcher.matches() && matcher.groupCount() == 2) {
                final String type = matcher.group(1);
                final String value = matcher.group(2);
                switch (type) {
                    case "code":
                        // value is a language code, look up
                        return languageCodeUtils.getLanguageNameForLanguageCode(value.toUpperCase());
                    case "name":
                        // value is the name to be shown
                        return value;
                }
            }

            // does not match expected pattern
            return null;
        }

    };

    /**
     * Converter for description field values that strips out any language code
     * prefix
     */
    private final FieldValueConverter descriptionConverter = new FieldValueConverter() {

        @Override
        public String getConvertedValue(String fieldValue, Locale locale) throws ConversionException {
            //For now, we simply ignore this information (see <https://trac.clarin.eu/ticket/780>)
            return fieldValue.replaceAll(FacetConstants.DESCRIPTION_LANGUAGE_PATTERN, "");
        }

    };

    private static class FieldDescriptiorValueConverter extends FieldValueConverter {

        private final Map<String, FieldValueDescriptor> fieldMap;

        public FieldDescriptiorValueConverter(Map<String, FieldValueDescriptor> fieldMap) {
            this.fieldMap = fieldMap;
        }

        @Override
        public String getConvertedValue(String value, Locale locale) {
            if (fieldMap.containsKey(value)) {
                return fieldMap.get(value).getDisplayValue();
            } else {
                return null;
            }
        }

    }

}
