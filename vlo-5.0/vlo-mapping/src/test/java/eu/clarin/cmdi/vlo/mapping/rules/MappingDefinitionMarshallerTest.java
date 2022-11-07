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

import eu.clarin.cmdi.vlo.mapping.rules.assertions.ContextAssertionBasedRule;
import static eu.clarin.cmdi.vlo.mapping.rules.MappingDefinitionSample.MAPPING_DEFINITION;
import static eu.clarin.cmdi.vlo.mapping.rules.MappingDefinitionSample.MAPPING_DEFINITION_XML_SOURCE;
import jakarta.xml.bind.JAXBException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MappingDefinitionMarshallerTest {

    private MappingDefinitionMarshaller instance;

    @BeforeEach
    public void setUp() throws JAXBException {
        instance = new MappingDefinitionMarshaller();
    }

    @Test
    public void testMarshall() throws Exception {
        log.info("Marshalling");

        try ( StringWriter writer = new StringWriter()) {
            try {
                final Result result = new StreamResult(writer);
                instance.marshal(MAPPING_DEFINITION, result);
                log.debug("Result: {}", writer.toString());
            } catch (JAXBException ex) {
                log.error("Failed to serialize", ex);
                throw ex;
            }

            log.info("Unmarshalling our own output");
            try ( StringReader reader = new StringReader(writer.toString())) {
                final MappingDefinition unmarshalled = instance.unmarshal(new StreamSource(reader));
                assertNotNull(unmarshalled);
                MappingDefinitionSample.assertContents(unmarshalled.getRules());
            }
        }
    }

    @Test
    public void testUnmarshall() throws Exception {
        log.info("Unmarshalling from static XML input");
        final MappingDefinition unmarshalled = instance.unmarshal(MAPPING_DEFINITION_XML_SOURCE());
        assertNotNull(unmarshalled);

        final List<ContextAssertionBasedRule> rules = unmarshalled.getRules();
        MappingDefinitionSample.assertContents(rules);
    }

}
