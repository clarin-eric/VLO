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
package eu.clarin.cmdi.vlo.mapping.rules;

import eu.clarin.cmdi.vlo.mapping.rules.assertions.ContextAssertionBooleanOperator;
import eu.clarin.cmdi.vlo.mapping.rules.assertions.ValueAssertion;
import eu.clarin.cmdi.vlo.mapping.rules.assertions.ConceptPathAssertion;
import eu.clarin.cmdi.vlo.mapping.rules.assertions.ContextAssertion;
import eu.clarin.cmdi.vlo.mapping.rules.assertions.ContextAssertionAndOperator;
import eu.clarin.cmdi.vlo.mapping.rules.assertions.XPathAssertion;
import eu.clarin.cmdi.vlo.mapping.rules.assertions.ContextAssertionNotOperator;
import eu.clarin.cmdi.vlo.mapping.rules.assertions.ContextAssertionBasedRule;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.mapping.rules.transformation.IdentityTransformer;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class MappingDefinitionSample {

    public static final MappingDefinition MAPPING_DEFINITION = new MappingDefinition();

    static {
        MAPPING_DEFINITION.setRules(Arrays.asList(// composite rule: and(not(false), {concept path})
                        new ContextAssertionBasedRule(
                                Arrays.asList(
                                        // and operator...
                                        new ContextAssertionAndOperator(
                                                new ContextAssertionNotOperator( // NOT
                                                        new ContextAssertionBooleanOperator(
                                                                Boolean.FALSE)), //FALSE
                                                new ConceptPathAssertion( // AND concept path
                                                        "concept1",
                                                        "concept2"))),
                                Arrays.asList(new IdentityTransformer("field1")),
                                true),
                        // multiple value rules
                        new ContextAssertionBasedRule(
                                Arrays.asList(
                                        new ValueAssertion("value1", Boolean.FALSE, Boolean.FALSE, "en"),
                                        new ValueAssertion("value2", Boolean.FALSE, Boolean.TRUE, "fr"),
                                        new ValueAssertion("value[A-Z]", Boolean.TRUE, Boolean.FALSE)
                                ),
                                Arrays.asList(new IdentityTransformer("field2")),
                                false),
                        // xpath rules
                        new ContextAssertionBasedRule(
                                Arrays.asList(
                                        new XPathAssertion("/path/to/the/element"),
                                        new XPathAssertion("/another/path")
                                ),
                                Arrays.asList(new IdentityTransformer("field3")),
                                true)
                ));

    }

    public static final String MAPPING_DEFINITION_XML = """
        <mappingDefinition xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <contextAssertionBasedRule>
                <assertions>
                    <assertion xsi:type="contextAssertionAndOperator">
                        <assertion xsi:type="contextAssertionNotOperator">
                            <assertion xsi:type="contextAssertionBooleanOperator">false</assertion>
                        </assertion>
                        <assertion xsi:type="conceptPathAssertion">
                            <conceptPath>concept1 concept2</conceptPath>
                        </assertion>
                    </assertion>
                </assertions>
                <transformers>
                    <transformer xsi:type="identityTransformation"/>
                </transformers>
                <terminal>true</terminal>
            </contextAssertionBasedRule>
            <contextAssertionBasedRule>
                <assertions>
                    <assertion xsi:type="valueAssertion" regex="false" lang="en" caseSensitive="false">value1</assertion>
                    <assertion xsi:type="valueAssertion" regex="false" lang="fr" caseSensitive="true">value2</assertion>
                    <assertion xsi:type="valueAssertion" regex="true" caseSensitive="false">value[A-Z]</assertion>
                </assertions>
                <transformers>
                    <transformer xsi:type="identityTransformation"/>
                </transformers>
                <terminal>false</terminal>
            </contextAssertionBasedRule>
            <contextAssertionBasedRule>
                <assertions>
                    <assertion xsi:type="xPathAssertion">/path/to/the/element</assertion>
                    <assertion xsi:type="xPathAssertion">/another/path</assertion>
                </assertions>
                <transformers>
                    <transformer xsi:type="identityTransformation"/>
                </transformers>
                <terminal>false</terminal>
            </contextAssertionBasedRule>
        </mappingDefinition>
        
      """;

    public static Source MAPPING_DEFINITION_XML_SOURCE() {
        return new StreamSource(new StringReader(MAPPING_DEFINITION_XML));
    }

    public static void assertContents(final Iterable<? extends MappingRule> rules) {
        assertContents(ImmutableList.copyOf(rules));
    }

    public static void assertContents(final List<? extends MappingRule> rules) {
        assertThat("Three rules in definition", rules, hasSize(3));

        assertThat("Rules have assertions",
                rules, hasItem(
                        hasProperty("assertions", hasSize(greaterThan(0)))));

        /**
         * Rule 1
         */
        {
            final ContextAssertionBasedRule rule1 = (ContextAssertionBasedRule) rules.get(0);
            final List<? extends ContextAssertion> rule1assertions = rule1.getAssertions();

            assertThat(rule1assertions, hasSize(1));
            assertThat("one 'root' assertion in rule 1",
                    rule1assertions, hasItem(
                            isA(ContextAssertionAndOperator.class)));

            final ContextAssertionAndOperator and = (ContextAssertionAndOperator) rule1assertions.get(0);
            assertThat("Number of assertions in rule",
                    and.getAssertions(), hasSize(2));
            assertThat("Types of assertions in rule",
                    and.getAssertions(), hasItems(
                    isA(ContextAssertionNotOperator.class),
                    isA(ConceptPathAssertion.class)));

            assertThat("Definition of concept path assertion",
                    and.getAssertions(), hasItem(
                    allOf(
                            isA(ConceptPathAssertion.class),
                            hasProperty("targetPath", hasItems("concept1", "concept2"))
                    )));

            assertThat("Terminal state of rule", rule1, hasProperty("terminal", equalTo(true)));
        }
        /**
         * Rule 2
         */
        {
            final ContextAssertionBasedRule rule2 = (ContextAssertionBasedRule) rules.get(1);
            final List<? extends ContextAssertion> rule2assertions = rule2.getAssertions();
            assertThat(rule2assertions, hasSize(3));
            assertThat("Only value assertions in rule 2", rule2assertions, allOf(
                    hasItem(isA(ValueAssertion.class)),
                    not(hasItem(not(isA(ValueAssertion.class))))));

            assertThat("Definition of value assertions", rule2assertions, hasItems(
                    allOf(
                            hasProperty("target", equalTo("value1")),
                            hasProperty("regex", equalTo(false)),
                            hasProperty("caseSensitive", equalTo(false)),
                            hasProperty("language", equalTo("en"))
                    ), allOf(
                            hasProperty("target", equalTo("value2")),
                            hasProperty("regex", equalTo(false)),
                            hasProperty("caseSensitive", equalTo(true)),
                            hasProperty("language", equalTo("fr"))
                    ), allOf(
                            hasProperty("target", equalTo("value[A-Z]")),
                            hasProperty("regex", equalTo(true)),
                            hasProperty("caseSensitive", equalTo(false)),
                            hasProperty("language", nullValue())
                    )
            ));

            assertThat("Terminal state of rule", rule2, hasProperty("terminal", equalTo(false)));
        }

        /**
         * Rule 3
         */
        {
            final ContextAssertionBasedRule rule3 = (ContextAssertionBasedRule) rules.get(2);
            final List<? extends ContextAssertion> rule3assertions = rule3.getAssertions();
            assertThat(rule3assertions, hasSize(2));
            assertThat("Only xpath assertions in rule 3", rule3assertions, allOf(
                    hasItem(isA(XPathAssertion.class)),
                    not(hasItem(not(isA(XPathAssertion.class))))));
        }
    }
}
/**
 * new ValueAssertion("value1", Boolean.FALSE, Boolean.FALSE, "en"), new
 * ValueAssertion("value2", Boolean.FALSE, Boolean.TRUE, "fr"), new
 * ValueAssertion("value[A-Z]", Boolean.TRUE, Boolean.FALSE)
 */
