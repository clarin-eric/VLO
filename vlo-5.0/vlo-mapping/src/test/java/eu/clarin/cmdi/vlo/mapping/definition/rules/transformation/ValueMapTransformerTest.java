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
package eu.clarin.cmdi.vlo.mapping.definition.rules.transformation;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.SimpleValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueContext;
import eu.clarin.cmdi.vlo.mapping.model.ValueLanguagePair;
import eu.clarin.cmdi.vlo.mapping.definition.rules.transformation.ValueMapTransformer.ValueMappingBehaviour;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class ValueMapTransformerTest {

    private final static String FIELD = "field";

    private final static Map<String, String> SIMPLE_VALUE_MAP = ImmutableMap.ofEntries(
            Maps.immutableEntry("a", "AA"),
            Maps.immutableEntry("b", "BB"),
            Maps.immutableEntry("c", "CC")
    );

    private final static Map<String, String> REGEX_MAP = ImmutableMap.ofEntries(
            Maps.immutableEntry("^a.*", "starts with a"),
            Maps.immutableEntry(".*z", "ends in z"),
            Maps.immutableEntry("[0-9]", "exactly one digit"),
            Maps.immutableEntry(".*[0-9].*$", "contains a number")
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
                        new ValueLanguagePair("a", "x"),
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
    public void testSimpleMapCaseInsensitiveAddOnMatch() {
        instance.setMap(SIMPLE_VALUE_MAP);
        instance.setCaseSensitive(false);
        instance.setRegex(false);
        instance.setTargetLang("en");
        instance.setBehaviour(ValueMappingBehaviour.ADD_ON_MATCH);

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("a", "x"),
                        new ValueLanguagePair("B", "fr"),
                        new ValueLanguagePair("c", null),
                        new ValueLanguagePair("d", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(7));

        assertThat("Original values should be included", result, hasItems(
                allOf(hasProperty("value", equalTo("a")), hasProperty("language", equalTo("x"))),
                allOf(hasProperty("value", equalTo("B")), hasProperty("language", equalTo("fr"))),
                allOf(hasProperty("value", equalTo("c"))),
                allOf(hasProperty("value", equalTo("d")), hasProperty("language", equalTo("x")))));

        assertThat("Full match including case should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("AA")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("CC")), hasProperty("language", equalTo("en")))));

        assertThat("Case insensitive match should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("BB")), hasProperty("language", equalTo("en")))));
    }

    /**
     * Test of apply method, of class ValueMapTransformer.
     */
    @Test
    public void testSimpleMapCaseInsensitiveRemoveOriginal() {
        instance.setMap(SIMPLE_VALUE_MAP);
        instance.setCaseSensitive(false);
        instance.setRegex(false);
        instance.setTargetLang("en");
        instance.setBehaviour(ValueMappingBehaviour.REMOVE_ORIGINAL);

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("a", "x"),
                        new ValueLanguagePair("B", "fr"),
                        new ValueLanguagePair("c", null),
                        new ValueLanguagePair("d", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(3));

        assertThat("Non-matching item should NOT be kept", result,
                not(hasItem(hasProperty("value", equalTo("d")))));

        assertThat("Full match including case should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("AA")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("BB")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("CC")), hasProperty("language", equalTo("en")))));
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
                        new ValueLanguagePair("a", "x"),
                        new ValueLanguagePair("B", "x"),
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
                allOf(hasProperty("value", equalTo("B")), hasProperty("language", equalTo("x"))),
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
                        new ValueLanguagePair("a", "x"),
                        new ValueLanguagePair("B", "x"),
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

    /**
     * Test of apply method, of class ValueMapTransformer.
     */
    @Test
    public void testRegexMapCaseInsensitiveDefaultBehaviour() {
        instance.setMap(REGEX_MAP);
        instance.setCaseSensitive(false);
        instance.setRegex(true);
        instance.setTargetLang("en");
        // note: default behaviour is 'replace on match'
        //instance.setBehaviour(ValueMappingBehaviour.REPLACE_ON_MATCH);

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("abcd", "x"),
                        new ValueLanguagePair("uvwxyz", "x"),
                        new ValueLanguagePair("oo33oo", null),
                        new ValueLanguagePair("dddd", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(4));

        assertThat("Regex match cases should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("starts with a")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("ends in z")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("contains a number")), hasProperty("language", equalTo("en")))));

        assertThat("Non-matching item should be kept (not mapped)", result, hasItems(
                allOf(hasProperty("value", equalTo("dddd")), hasProperty("language", equalTo("x")))));
    }

    /**
     * Test of apply method, of class ValueMapTransformer.
     */
    @Test
    public void testRegexMapCaseSensitive() {
        instance.setMap(REGEX_MAP);
        instance.setCaseSensitive(true);
        instance.setRegex(true);
        instance.setTargetLang("en");

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("abcd", "x"),
                        new ValueLanguagePair("ABCD", "x"),
                        new ValueLanguagePair("uvwxyz", "x"),
                        new ValueLanguagePair("UVWXYZ", "x"),
                        new ValueLanguagePair("oo33oo", null),
                        new ValueLanguagePair("dddd", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(6));

        assertThat("Regex match cases should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("starts with a")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("ends in z")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("contains a number")), hasProperty("language", equalTo("en")))));

        assertThat("Non-matching item should be kept (not mapped)", result, hasItems(
                allOf(hasProperty("value", equalTo("ABCD")), hasProperty("language", equalTo("x"))),
                allOf(hasProperty("value", equalTo("UVWXYZ")), hasProperty("language", equalTo("x"))),
                allOf(hasProperty("value", equalTo("dddd")), hasProperty("language", equalTo("x")))));
    }

    /**
     * Test of apply method, of class ValueMapTransformer.
     */
    @Test
    public void testRegexMapCaseInsensitiveWithDefault() {
        instance.setMap(REGEX_MAP);
        instance.setCaseSensitive(false);
        instance.setRegex(true);
        instance.setTargetLang("en");
        instance.setDefaultValue("defaultValue");

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(ImmutableList.of(
                        new ValueLanguagePair("abcd", "x"),
                        new ValueLanguagePair("uvwxyz", "x"),
                        new ValueLanguagePair("oo33oo", null),
                        new ValueLanguagePair("dddd", "x")))
                .build();

        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        assertNotNull(resultStream);

        // collect
        final List<ValueLanguagePair> result = resultStream.toList();
        assertThat(result, hasSize(4));

        assertThat("Regex match cases should be mapped", result, hasItems(
                allOf(hasProperty("value", equalTo("starts with a")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("ends in z")), hasProperty("language", equalTo("en"))),
                allOf(hasProperty("value", equalTo("contains a number")), hasProperty("language", equalTo("en")))));

        assertThat("Non-matching item should be mapped to default value", result, hasItems(
                allOf(hasProperty("value", equalTo("defaultValue")), hasProperty("language", equalTo("en")))));
    }

    @Test
    public void testValueMappingBehaviourEnum() {
        assertEquals(ValueMappingBehaviour.ADD_ON_MATCH, ValueMappingBehaviour.fromValue("addOnMatch"));
        assertEquals("addOnMatch", ValueMappingBehaviour.ADD_ON_MATCH.value());

        assertEquals(ValueMappingBehaviour.REMOVE_ORIGINAL, ValueMappingBehaviour.fromValue("removeOriginal"));
        assertEquals("removeOriginal", ValueMappingBehaviour.REMOVE_ORIGINAL.value());

        assertEquals(ValueMappingBehaviour.REPLACE_ON_MATCH, ValueMappingBehaviour.fromValue("replaceOnMatch"));
        assertEquals("replaceOnMatch", ValueMappingBehaviour.REPLACE_ON_MATCH.value());

        assertThrows(IllegalArgumentException.class, () -> ValueMappingBehaviour.fromValue("somethingElse"));
    }

    @Test
    public void testGetScore() {
        // no score
        instance = new ValueMapTransformer(FIELD);
        assertEquals(5, instance.getScore(5));
        // override
        instance = new ValueMapTransformer(FIELD, 10);
        assertEquals(10, instance.getScore(5));
    }

    @Test
    @Disabled
    public void regexPerformanceTest() {
        final int valuesCount = 1_000_000;

        instance.setMap(REGEX_MAP);
        instance.setCaseSensitive(false);
        instance.setRegex(false);
        instance.setTargetLang("en");

        final ImmutableList.Builder<ValueLanguagePair> listBuilder
                = ImmutableList.<ValueLanguagePair>builder();
        final Random random = new Random();
        for (int i = 0; i < valuesCount / 2; i++) {
            listBuilder.add(new ValueLanguagePair(Integer.toString(random.nextInt()), "en"));
            listBuilder.add(new ValueLanguagePair("abc", "en"));
        }

        final ValueContext valueContext = SimpleValueContext.builder()
                .values(listBuilder.build())
                .build();

        // (time this)
        Stopwatch stopwatch = Stopwatch.createStarted();
        final Stream<ValueLanguagePair> resultStream = instance.apply(valueContext, mappingConfig);
        final List<ValueLanguagePair> result = resultStream.toList();
        stopwatch.stop();
        log.info("Done timing {} values -> {} values in {}ms", valuesCount, result.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

}
