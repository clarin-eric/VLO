/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.PIDUtils;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * Converter that renders short versions of landing page links (handle or
 * hostname)
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class LandingPageShortLinkLabelConverter implements IConverter<String> {

    private static final Pattern URL_HOST_PATTERN = Pattern.compile("^https?:\\/\\/([^\\\\/]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public String convertToObject(String value, Locale locale) throws ConversionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String convertToString(String value, Locale locale) {
        if (PIDUtils.isHandle(value)) {
            return PIDUtils.getSchemeSpecificId(value);
        } else {
            //Find host in URL
            final Matcher matcher = URL_HOST_PATTERN.matcher(value);
            if (matcher.find() && matcher.groupCount() > 0) {
                return matcher.group(1);
            } else {
                return value;
            }
        }
    }

}
