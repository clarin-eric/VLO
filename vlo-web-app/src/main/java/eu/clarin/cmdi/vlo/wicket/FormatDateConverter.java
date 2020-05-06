/*
 * Copyright (C) 2020 CLARIN
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

import java.text.DateFormat;
import java.util.Locale;
import org.apache.wicket.util.convert.converter.DateConverter;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FormatDateConverter extends DateConverter {

    private final int dateFormat;

    /**
     *
     * @param dateFormat date format from {@link DateFormat} constants,e.g.
     * {@link DateFormat#SHORT}
     */
    public FormatDateConverter(int dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public DateFormat getDateFormat(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }

        // return a clone because DateFormat.getDateInstance uses a pool
        return (DateFormat) DateFormat.getDateInstance(dateFormat, locale).clone();
    }

}
