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
package eu.clarin.cmdi.vlo.service.impl;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.facets.FacetsConfigurationsMarshaller;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.impl.FacetConditionEvaluationServiceImpl.FacetDisplayCondition;
import eu.clarin.cmdi.vlo.service.impl.FacetConditionEvaluationServiceImpl.FacetsConfigurationConditionsConverter;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FacetConditionEvaluationServiceImplTest {

    /**
     * Test of FacetsConfigurationConditionsConverter
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testcreateConditionsMap() throws Exception {
        final FacetsConfigurationConditionsConverter converter = new FacetsConfigurationConditionsConverter();

        final String configXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<facetsConfiguration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "    <facet name=\"multilingual\">"
                + "        <allowMultipleValues>false</allowMultipleValues>"
                + "        <multilingual>true</multilingual>"
                + "        <caseInsensitive>false</caseInsensitive>"
                + "        <description>The name of the resource or tool</description>"
                + "        <definition>The full public name of the resource or tool</definition>"
                + "        <conditions>"
                + "            <condition>"
                + "                <facetSelectionCondition>"
                + "                    <facetName>language</facetName>"
                + "                    <selection type=\"anyValue\"/>"
                + "                </facetSelectionCondition>"
                + "            </condition>"
                + "        </conditions>"
                + "        <conditions>"
                + "            <condition>"
                + "                <facetSelectionCondition>"
                + "                    <facetName>testA</facetName>"
                + "                    <selection type=\"anyOf\">"
                + "                        <value>test1</value>"
                + "                        <value>test2</value>"
                + "                    </selection>"
                + "                </facetSelectionCondition>"
                + "            </condition>"
                + "            <condition>"
                + "                <facetSelectionCondition>"
                + "                    <facetName>testB</facetName>"
                + "                    <selection type=\"allOf\">"
                + "                        <value>test1</value>"
                + "                        <value>test2</value>"
                + "                    </selection>"
                + "                </facetSelectionCondition>"
                + "            </condition>"
                + "        </conditions>"
                + "    </facet>"
                + "    <facet name=\"test1\">"
                + "        <multilingual>false</multilingual>"
                + "        <caseInsensitive>true</caseInsensitive>"
                + "        <description>Test description</description>"
                + "        <definition>Test definition</definition>"
                + "        <conditions>"
                + "            <condition>"
                + "                <facetSelectionCondition>"
                + "                    <facetName>testC</facetName>"
                + "                    <selection type=\"anyValue\"/>"
                + "                </facetSelectionCondition>"
                + "            </condition>"
                + "        </conditions>"
                + "    </facet>"
                + "    <facet name=\"test2\">"
                + "        <multilingual>false</multilingual>"
                + "        <caseInsensitive>true</caseInsensitive>"
                + "        <description>Test description</description>"
                + "        <definition>Test definition</definition>"
                + "    </facet>"
                + "</facetsConfiguration>"
                + "";
        final FacetsConfiguration config = unmarshalConfig(configXml);

        final Map<String, FacetConditionEvaluationServiceImpl.FacetDisplayCondition> result
                = converter.convert(config);

        assertThat(result, is(notNullValue()));
        assertThat(result.entrySet(), hasSize(2));
        assertThat(result.get("test1"), isA(FacetDisplayCondition.class));
        assertThat(result.get("multilingual"), isA(FacetDisplayCondition.class));

        final FacetDisplayCondition multilingualFacetCondition = result.get("multilingual");

        {
            //empty selection
            final List<FacetField> facetFields = Collections.emptyList();
            final QueryFacetsSelection selection = new QueryFacetsSelection();

            final boolean evaluation = multilingualFacetCondition.evaluate(selection, facetFields);
            assertFalse("Empty selection should not match conditions", evaluation);
        }

        {
            //any language value should match
            final List<FacetField> facetFields = Collections.emptyList();
            final QueryFacetsSelection selection = new QueryFacetsSelection();
            selection.addSingleFacetValue("language", FacetSelectionType.AND, ImmutableList.of("test"));

            final boolean evaluation = multilingualFacetCondition.evaluate(selection, facetFields);
            assertTrue("Any value for language facet should match conditions", evaluation);
        }

        {
            //any language value should match
            final List<FacetField> facetFields = Collections.emptyList();
            final QueryFacetsSelection selection = new QueryFacetsSelection();
            selection.addSingleFacetValue("testA", FacetSelectionType.AND, ImmutableList.of("test1"));
            assertFalse("Accepted value for testA: only partial match", multilingualFacetCondition.evaluate(selection, facetFields));

            selection.addSingleFacetValue("testB", FacetSelectionType.AND, ImmutableList.of("test1"));
            assertFalse("One out of two required values for testB: only partial match", multilingualFacetCondition.evaluate(selection, facetFields));

            selection.addNewFacetValue("testB", FacetSelectionType.AND, ImmutableList.of("test2"));
            assertTrue("Two out of two required values for testB: should match conditions", multilingualFacetCondition.evaluate(selection, facetFields));

            selection.removeFacetSelection("testA");
            assertFalse("Missing accepted value for testA: only partial match", multilingualFacetCondition.evaluate(selection, facetFields));
        }

    }

    private FacetsConfiguration unmarshalConfig(final String configString) throws JAXBException {
        return new FacetsConfigurationsMarshaller()
                .unmarshal(new StreamSource(new ByteArrayInputStream(configString.getBytes())));
    }

}
