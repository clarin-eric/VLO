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

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.mapping.processing.IdentityTransformation;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class MappingDefinitionSample {

    public static final MappingDefinition MAPPING_DEFINITION = new MappingDefinition();

    static {
        MAPPING_DEFINITION.setRules(
                Arrays.asList(
                        new ContextAssertionBasedRule(
                                Arrays.asList(
                                        new ConceptPathAssertion(
                                                "concept1",
                                                "concept2")),
                                Arrays.asList(
                                        new IdentityTransformation()),
                                true
                        )));

    }

    public static final String MAPPING_DEFINITION_XML = """
        <mappingDefinition>
            <contextAssertionBasedRule>
                <assertions>
                    <assertion xsi:type="conceptPathAssertion" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                        <conceptPath>concept1 concept2</conceptPath>
                    </assertion>
                </assertions>
                <transformations>
                    <transformation xsi:type="identityTransformation" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"/>
                </transformations>
                <terminal>true</terminal>
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
        assertThat(rules, hasSize(1));
        assertThat(rules, hasItem(
                hasProperty("assertions",
                        hasItem(isA(ConceptPathAssertion.class)))));
        assertThat(rules, hasItem(
                hasProperty("assertions",
                        hasItem(
                                hasProperty("targetPath", hasItems("concept1", "concept2"))))));
        assertThat(rules, hasItem(
                hasProperty("terminal", equalTo(true))));
    }
}
