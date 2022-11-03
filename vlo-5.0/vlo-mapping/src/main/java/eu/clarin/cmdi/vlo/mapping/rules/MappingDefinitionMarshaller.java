package eu.clarin.cmdi.vlo.mapping.rules;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

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
/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class MappingDefinitionMarshaller {

    private final JAXBContext jaxbContext;

    public MappingDefinitionMarshaller() throws JAXBException {
        jaxbContext = JAXBContext.newInstance(
                MappingDefinition.class, ConceptPathAssertion.class, ContextAssertionBasedRule.class);
    }

    public void marshal(MappingDefinition definition, Writer writer) throws JAXBException, IOException {

        Marshaller mar = jaxbContext.createMarshaller();
        mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        mar.marshal(definition, writer);
    }

    public MappingDefinition unmarshal(Reader reader) throws JAXBException {
        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        final Object result = jaxbUnmarshaller.unmarshal(reader);
        if (result instanceof MappingDefinition mappingDefinition) {
            return mappingDefinition;
        } else {
            log.error("Unmarshalled object is of type {} (expecting MappingDefinition): {}", result == null ? "null" : result.getClass(), result);
            return null;
        }
    }
}
