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
package eu.clarin.cmdi.vlo.mapping.rules.transformation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(MockitoExtension.class)
public class ValueMapTransformerTest {

    private final static String FIELD = "field";

    private final static Map<String, String> SIMPLE_VALUE_MAP = ImmutableMap.ofEntries(
            Maps.immutableEntry("a", "AA"),
            Maps.immutableEntry("b", "BB"),
            Maps.immutableEntry("c", "CC")
    );
    
    @Mock
    private VloMappingConfiguration mappingConfig;

    private ValueMapTransformer instance;

    @BeforeEach
    public void setUp() {
        instance = new ValueMapTransformer();
        instance.setField(FIELD);
    }

    /**
     * Test of apply method, of class ValueMapTransformer.
     */
    @Test
    public void testSimpleMapCaseInsensitive() {
        instance.setMap(SIMPLE_VALUE_MAP);
        instance.setCaseSensitive(false);
        instance.setRegex(false);
        instance.setTargetLang("en");

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("a", "nl"),
                        new ValueLanguagePair("B", "fr"),
                        new ValueLanguagePair("c", null),
                        new ValueLanguagePair("d", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(4));

        assertThat("Full match including case should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("AA")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("CC")), hasProperty("language", equalTo("en")))));

        assertThat("Case insensitive match should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("BB")), hasProperty("language", equalTo("en")))));

        assertThat("Non-matching item should be kept (not mapped)", result, hasItems(
                allOf(hasProperty("value", equalTo("d")), hasProperty("language", equalTo("x")))));
    }

    /**
     * Test of apply method, of class ValueMapTransformer.
     */
    @Test
    public void testSimpleMapCaseSensitive() {
        instance.setMap(SIMPLE_VALUE_MAP);
        instance.setCaseSensitive(true);
        instance.setRegex(false);
        instance.setTargetLang("en");

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("a", "nl"),
                        new ValueLanguagePair("B", "fr"),
                        new ValueLanguagePair("C", null),
                        new ValueLanguagePair("d", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(4));

        assertThat("Full match including case should be mapped", result, hasItem(
                allOf(hasProperty("value", equalTo("AA")), hasProperty("language", equalTo("en")))));

        assertThat("Non-matching items should be kept (not mapped)", result, hasItems(
                allOf(hasProperty("value", equalTo("B")), hasProperty("language", equalTo("fr"))),
                allOf(hasProperty("value", equalTo("C"))),
                allOf(hasProperty("value", equalTo("d")), hasProperty("language", equalTo("x")))));
    }

    /**
     * Test of apply method, of class ValueMapTransformer.
     */
    @Test
    public void testSimpleMapCaseInsensitiveWithDefaultValue() {
        instance.setMap(SIMPLE_VALUE_MAP);
        instance.setCaseSensitive(false);
        instance.setRegex(false);
        instance.setTargetLang("en");
        instance.setDefaultValue("defaultValue");

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("a", "nl"),
                        new ValueLanguagePair("B", "fr"),
                        new ValueLanguagePair("c", null),
                        new ValueLanguagePair("d", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(4));

        assertThat("Case insensitive matches should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("AA")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("BB")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("CC")), hasProperty("language", equalTo("en")))));

        assertThat("Non-matching item should be mapped to default value", result, hasItems(
                allOf(hasProperty("value", equalTo("defaultValue")), hasProperty("language", equalTo("en")))));
    }
    
    

}
