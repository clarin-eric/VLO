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

import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.VloMappingTestConfiguration;
import eu.clarin.cmdi.vlo.mapping.processing.IdentityTransformation;
import jakarta.xml.bind.JAXBException;
import java.io.StringWriter;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MappingDefinitionMarshallerTest {

    private static MappingDefinition definition;

    @BeforeAll
    public static void setUpClass() {
        definition = new MappingDefinition();
        definition.setRules(
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

    @Test
    public void testWriteRules() throws Exception {
        StringWriter writer = new StringWriter();
        MappingDefinitionMarshaller instance = new MappingDefinitionMarshaller();
        try {
            instance.marshal(definition, writer);
            log.info(writer.toString());
        } catch (JAXBException ex) {
            log.error("Failed to serialize", ex);
            throw ex;
        }

        final String xml = writer.toString();
    }

}
