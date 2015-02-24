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
package eu.clarin.cmdi.vlo.pojo;

import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import java.io.Serializable;
import java.util.regex.Pattern;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.util.convert.IConverter;

/**
 * Defines a filter for field values (designed to be used by
 * {@link FacetFieldValuesProvider})
 *
 * @author twagoo
 */
public class FieldValuesFilter implements Serializable {

    private String name;
    private Pattern namePattern;
    private int minimalOccurence;

    public String getName() {
        return name;
    }

    /**
     *
     * @param name string that matches should <em>contain</em>
     */
    public void setName(String name) {
        this.name = name;
        this.namePattern = createNamePattern(name);
    }

    public int getMinimalOccurence() {
        return minimalOccurence;
    }

    /**
     *
     * @param minimalOccurence minimal number of occurrences matches should have
     */
    public void setMinimalOccurence(int minimalOccurence) {
        this.minimalOccurence = minimalOccurence;
    }

    /**
     *
     * @param count count (name + count) to check
     * @param converter optional converter to apply to value before comparison
     * @return true IFF the {@link Count#getCount() } is more than {@link #getMinimalOccurence()
     * } {@link Count#getName() } contains {@link #getName() } (case
     * insensitive)
     */
    public boolean matches(Count count, IConverter<String> converter) {
        if (count.getCount() >= minimalOccurence) {
            if (namePattern == null) {
                // no pattern to compare to, always matches
                return true;
            } else {
                // convert value if converter is provided
                final String value;
                if (converter == null) {
                    value = count.getName();
                } else {
                    value = converter.convertToString(count.getName(), null);
                }
                return namePattern.matcher(value).find();
            }
        } else {
            // too few occurences, no match
            return false;
        }
    }

    public boolean isEmpty() {
        return minimalOccurence == 0 && (name == null || name.isEmpty());
    }

    private Pattern createNamePattern(String name) {
        if (name == null) {
            return null;
        } else {
            // make a matching pattern for the name (case insensitive, not parsing RegEx syntax and supporting unicode)
            return Pattern.compile(name, Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
        }
    }
}
