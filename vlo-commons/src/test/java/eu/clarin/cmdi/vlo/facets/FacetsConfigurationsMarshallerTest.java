/*
 * Copyright (C) 2021 CLARIN
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
package eu.clarin.cmdi.vlo.facets;

import com.google.common.base.Objects;
import eu.clarin.cmdi.vlo.facets.configuration.Conditions;
import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.facets.configuration.FacetCondition;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.facets.configuration.Selection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.hamcrest.TypeSafeMatcher;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FacetsConfigurationsMarshallerTest {

    public FacetsConfigurationsMarshallerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

//    /**
//     * Test of marshal method, of class FacetsConfigurationsMarshaller.
//     */
//    @Test
//    public void testMarshal() throws Exception {
//        System.out.println("marshal");
//        FacetsConfiguration config = null;
//        Result result_2 = null;
//        FacetsConfigurationsMarshaller instance = new FacetsConfigurationsMarshaller();
//        instance.marshal(config, result_2);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of unmarshal method, of class FacetsConfigurationsMarshaller.
     */
    @Test
    public void testUnmarshal() throws Exception {
        System.out.println("unmarshal");
        try (InputStream in = getClass().getResourceAsStream("/facetsConfiguration-test.xml")) {
            try (InputStreamReader reader = new InputStreamReader(in)) {
                Source source = new StreamSource(reader);
                FacetsConfigurationsMarshaller instance = new FacetsConfigurationsMarshaller();
                FacetsConfiguration result = instance.unmarshal(source);
                assertNotNull(result);
                final List<Facet> facets = result.getFacet();
                assertNotNull(facets);
                assertThat(facets, hasSize(2));
                assertThat(facets, hasItem(
                        allOf(
                                hasProperty("name", equalTo("multilingual")),
                                hasMultilingualValue(true),
                                hasCaseInsensitiveValue(false),
                                hasAllowMultipleValue(false),
                                hasProperty("description", notNullValue()),
                                hasProperty("definition", notNullValue()),
                                hasProperty("conditions", hasSize(2))
                        )
                ));
                assertThat("Conditions of multilingual", facets, hasItem(allOf(
                        hasProperty("name", equalTo("multilingual")),
                        hasProperty("conditions", allOf(
                                hasItem(allOf(
                                        isA(Conditions.class),
                                        hasProperty("condition", allOf(
                                                isA(List.class),
                                                hasItem(
                                                        hasProperty("facetCondition", allOf(
                                                                isA(FacetCondition.class),
                                                                hasProperty("facetName", equalTo("language")),
                                                                hasProperty("selection", isA(Selection.class))
                                                        ))
                                                )
                                        ))
                                ))
                        ))
                )));
                assertThat("No conditions for test facet", facets, hasItem(allOf(
                        hasProperty("name", equalTo("test")),
                        hasMultilingualValue(false),
                        hasCaseInsensitiveValue(true),
                        hasAllowMultipleValue(null),
                        hasProperty("description", equalTo("Test description")),
                        hasProperty("definition", equalTo("Test definition")),
                        hasProperty("conditions", hasSize(0))
                )));
            }
        }
    }

    private static Matcher<Facet> hasMultilingualValue(final Boolean value) {
        return hasBooleanValue("Multilingual", Facet::isMultilingual, value);
    }

    private static Matcher<Facet> hasAllowMultipleValue(final Boolean value) {
        return hasBooleanValue("Allow multiple", Facet::isAllowMultipleValues, value);
    }

    private static Matcher<Facet> hasCaseInsensitiveValue(final Boolean value) {
        return hasBooleanValue("Case insensitiveness", Facet::isCaseInsensitive, value);
    }

    private static Matcher<Facet> hasBooleanValue(String name, Function<Facet, Boolean> supplier, final Boolean value) {
        return new TypeSafeMatcher<Facet>(Facet.class) {
            private Boolean actual;

            @Override
            protected boolean matchesSafely(Facet item) {
                if (item == null) {
                    return false;
                } else {
                    this.actual = supplier.apply(item);
                    return Objects.equal(value, actual);
                }
            }

            @Override
            public void describeTo(Description d) {
                d.appendText(String.format("%s value not as expected: %b instead of %b", name, actual, value));
            }
        };

    }
}
