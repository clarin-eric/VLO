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

import eu.clarin.cmdi.vlo.facets.FacetsConfigurationsMarshaller;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.service.impl.FacetConditionEvaluationServiceImpl.FacetDisplayCondition;
import eu.clarin.cmdi.vlo.service.impl.FacetConditionEvaluationServiceImpl.FacetsConfigurationConditionsConverter;
import java.io.ByteArrayInputStream;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FacetConditionEvaluationServiceImplTest {

    /**
     * Test of FacetsConfigurationConditionsConverter
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
                + "                <facetCondition>"
                + "                    <facetName>language</facetName>"
                + "                    <selection type=\"anyValue\"/>"
                + "                </facetCondition>"
                + "            </condition>"
                + "            <condition>"
                + "                <facetCondition>"
                + "                    <facetName>test</facetName>"
                + "                    <selection type=\"anyOf\">"
                + "                        <value>test1</value>"
                + "                        <value>test2</value>"
                + "                    </selection>"
                + "                </facetCondition>"
                + "            </condition>"
                + "        </conditions>"
                + "        <conditions>"
                + "            <condition>"
                + "                <facetCondition>"
                + "                    <facetName>language</facetName>"
                + "                    <selection type=\"anyValue\"/>"
                + "                </facetCondition>"
                + "            </condition>"
                + "        </conditions>"
                + "    </facet>"
                + "    <facet name=\"test\">"
                + "        <multilingual>false</multilingual>"
                + "        <caseInsensitive>true</caseInsensitive>"
                + "        <description>Test description</description>"
                + "        <definition>Test definition</definition>"
                + "    </facet>"
                + "</facetsConfiguration>"
                + "";
        final FacetsConfiguration config = unmarshalConfig(configXml);

        final Map<String, FacetConditionEvaluationServiceImpl.FacetDisplayCondition> result
                = converter.createConditionsMap(config);

        assertThat(result, is(notNullValue()));
        assertThat(result.entrySet(), hasSize(1));
        
        final FacetDisplayCondition condition = result.get(0);
        
    }

    private FacetsConfiguration unmarshalConfig(final String configString) throws JAXBException {
        return new FacetsConfigurationsMarshaller()
                .unmarshal(new StreamSource(new ByteArrayInputStream(configString.getBytes())));
    }

}
