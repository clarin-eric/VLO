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
import java.util.Locale;
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

    public FieldValueConverterProviderImpl(LanguageCodeUtils languageCodeUtils) {
        this.languageCodeUtils = languageCodeUtils;
    }

    @Override
    public IConverter<String> getConverter(String fieldName) {
        switch (fieldName) {
            case FacetConstants.FIELD_LANGUAGE_CODE:
                return languageCodeConverter;
            case FacetConstants.FIELD_DESCRIPTION:
                return descriptionConverter;
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
            throw new UnsupportedOperationException("Not supported");
        }

    }

    /**
     * Converter for language code/name strings (following the pattern of
     * {@link #LANGUAGE_CODE_PATTERN}) into language names
     *
     * @see LanguageCodeUtils#getLanguageNameForLanguageCode(java.lang.String)
     */
    private final FieldValueConverter languageCodeConverter = new FieldValueConverter() {

        @Override
        public String convertToString(String fieldValue, Locale locale) throws ConversionException {
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

            // does not match expected pattern, return original value
            return fieldValue;
        }

    };

    /**
     * Converter for description field values that strips out any language code
     * prefix
     */
    private final FieldValueConverter descriptionConverter = new FieldValueConverter() {

        @Override
        public String convertToString(String fieldValue, Locale locale) throws ConversionException {
            return fieldValue.replaceAll(FacetConstants.DESCRIPTION_LANGUAGE_PATTERN, "");
        }

    };

}
