/*
 * Copyright (C) 2022 CLARIN ERIC <clarin@clarin.eu>
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
package eu.clarin.cmdi.vlo.mapping.rules.assertion;

import eu.clarin.cmdi.vlo.mapping.rules.assertions.ValueAssertion;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ValueAssertionTest {

    ValueContext context = SimpleValueContext.builder()
            .values(ImmutableList.of(
                    new ValueLanguagePair("value1", "lang1"),
                    new ValueLanguagePair("value2", "lang2"),
                    new ValueLanguagePair("value3", "lang3")
            ))
            .build();

    /**
     * Test of evaluate method, of class ValueAssertion.
     */
    @Test
    public void testCaseSensitiveValueMatch() {
        for (String value : ImmutableList.of("value1", "value2", "value3")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.FALSE, Boolean.TRUE);
            assertTrue(instance.evaluate(context), "Case sensitive value match: " + value);
        }
        for (String value : Arrays.asList("value4", "")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.FALSE, Boolean.TRUE);
            assertFalse(instance.evaluate(context), "Case sensitive value mismatch: " + value);
        }
    }

    /**
     * Test of evaluate method, of class ValueAssertion.
     */
    @Test
    public void testCaseInsensitiveValueMatch() {
        for (String value : ImmutableList.of("value1", "value2", "value3", "VALUE1", "Value2", "vALUE3")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.FALSE, Boolean.FALSE);
            assertTrue(instance.evaluate(context), "Case insensitive value match: " + value);
        }
        for (String value : ImmutableList.of("VALUE4", "Value4", "vALUE4")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.FALSE, Boolean.FALSE);
            assertFalse(instance.evaluate(context), "Case insensitive value mismatch: " + value);
        }
    }

    /**
     * Test of evaluate method, of class ValueAssertion.
     */
    @Test
    public void testRegexMatch() {
        for (String value : ImmutableList.of("value1", "^value.+", "value.$", "[Vv][^o]lue[0-9]", "(value1|value2)")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.TRUE, Boolean.TRUE);
            assertTrue(instance.evaluate(context), "Regex match: " + value);
        }
        for (String value : ImmutableList.of("VALUE1", "value[4-9]", "value", "value$")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.TRUE, Boolean.TRUE);
            assertFalse(instance.evaluate(context), "Regex mismatch: " + value);
        }
    }

    /**
     * Test of evaluate method, of class ValueAssertion.
     */
    @Test
    public void testCaseInsensitiveRegexMatch() {
        for (String value : ImmutableList.of("value1", "VALUE1", "^value.+", "^Value.+", "value.$", "[Vv][^o]lue[0-9]", "(value1|value2)")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.TRUE, Boolean.FALSE);
            assertTrue(instance.evaluate(context), "Regex match: " + value);
        }
        for (String value : ImmutableList.of("value[4-9]", "value", "value$")) {
            final ValueAssertion instance = new ValueAssertion(value, Boolean.TRUE, Boolean.FALSE);
            assertFalse(instance.evaluate(context), "Regex mismatch: " + value);
        }
    }

    /**
     * Test of evaluate method, of class ValueAssertion.
     */
    @Test
    public void testLanguageMatch() {
        for (String[] values : ImmutableList.of(new String[]{"value1", "lang1"}, new String[]{"value2", "LANG2"}, new String[]{"value3", "Lang3"})) {
            final String value = values[0];
            final String language = values[1];
            final ValueAssertion instance = new ValueAssertion(value, Boolean.FALSE, Boolean.TRUE, language);
            assertTrue(instance.evaluate(context), "Case sensitive langage/value match: " + value + " " + language);
        }
        for (String[] values : ImmutableList.of(new String[]{"value1", "lang2"}, new String[]{"value3", "LANG"}, new String[]{"value", "lang1"})) {
            final String value = values[0];
            final String language = values[1];
            final ValueAssertion instance = new ValueAssertion(value, Boolean.FALSE, Boolean.TRUE, language);
            assertFalse(instance.evaluate(context), "Case sensitive langage/value mismatch: " + value + " " + language);
        }
    }
}
