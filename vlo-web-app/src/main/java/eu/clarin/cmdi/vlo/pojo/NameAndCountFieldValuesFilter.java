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
public class NameAndCountFieldValuesFilter implements FieldValuesFilter, Serializable {

    public static final char ANY_CHARACTER_SYMBOL = '*';
    public static final char NON_ALPHABETICAL_CHARACTER_SYMBOL = '?';

    private String name;
    private Character firstCharacter;
    private Pattern namePattern;
    private Integer minimalOccurence;

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

    public Integer getMinimalOccurence() {
        return minimalOccurence;
    }

    /**
     *
     * @param minimalOccurence minimal number of occurrences matches should have
     */
    public void setMinimalOccurence(Integer minimalOccurence) {
        this.minimalOccurence = minimalOccurence;
    }

    public Character getFirstCharacter() {
        return firstCharacter;
    }

    public void setFirstCharacter(Character firstCharacter) {
        this.firstCharacter = firstCharacter;
    }

    /**
     *
     * @param count count (name + count) to check
     * @param converter optional converter to apply to value before comparison
     * @return true IFF the {@link Count#getCount() } is more than {@link #getMinimalOccurence()
     * } {@link Count#getName() } contains {@link #getName() } (case
     * insensitive)
     */
    @Override
    public boolean matches(Count count, IConverter<String> converter) {
        if (minimalOccurence == null || matchesOccurrences(count)) {
            if (firstCharacter == null && namePattern == null) {
                return true;
            } else {
                // convert value if converter is provided
                final String convertedValue;
                if (converter == null) {
                    convertedValue = count.getName();
                } else {
                    convertedValue = converter.convertToString(count.getName(), null);
                }
                if (firstCharacter == null || matchesFirstCharacter(convertedValue)) {
                    return (namePattern == null || matchesName(convertedValue));
                }
            }
        }
        // no match
        return false;
    }

    private boolean matchesOccurrences(Count count) {
        return count.getCount() >= minimalOccurence;
    }

    private boolean matchesFirstCharacter(String value) {
        if (value.isEmpty()) {
            return firstCharacter == null;
        } else if (firstCharacter.equals(ANY_CHARACTER_SYMBOL)) {
            return true;
        } else {
            final Character valueFirstChar = value.charAt(0);
            if (firstCharacter.equals(NON_ALPHABETICAL_CHARACTER_SYMBOL)) {
                return !Character.isAlphabetic(valueFirstChar);
            } else {
                return Character.valueOf(Character.toUpperCase(firstCharacter)).equals(Character.toUpperCase(valueFirstChar));
            }
        }
    }

    private boolean matchesName(String value) {
        return namePattern.matcher(value).find();
    }

    @Override
    public boolean isEmpty() {
        return (minimalOccurence == null || minimalOccurence == 0) && (name == null || name.isEmpty()) && firstCharacter == null;
    }

    private Pattern createNamePattern(String name) {
        if (name == null) {
            return null;
        } else {
            // make a matching pattern for the name (case insensitive, not parsing RegEx syntax and supporting unicode)
            return Pattern.compile(name, Pattern.LITERAL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
        }
    }

    @Override
    public NameAndCountFieldValuesFilter copy() {
        final NameAndCountFieldValuesFilter copy = new NameAndCountFieldValuesFilter();
        copy.setFirstCharacter(firstCharacter);
        copy.setMinimalOccurence(minimalOccurence);
        copy.setName(name);
        return copy;
    }

}
