/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.config;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes and deserializes {@link VloConfig} objects to/from XML files using
 * the Java Architecture for XML Binding (JAXB)
 *
 * @author twagoo
 */
public class VloConfigMarshaller {

    private final static Logger _logger = LoggerFactory.getLogger(VloConfigMarshaller.class);

    private final JAXBContext jc;

    public VloConfigMarshaller() throws JAXBException {
        this.jc = JAXBContext.newInstance(VloConfig.class);
    }

    /**
     * Marshals (serializes) an existing configuration to some output location
     *
     * @param config configuration to marshal
     * @param result output result for the marshalling, e.g. a
     * {@link StreamResult} for usage with Files, Streams or Writers
     * @throws JAXBException
     */
    public final void marshal(VloConfig config, Result result) throws JAXBException {
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(config, result);
    }

    /**
     * Unmarshals (deserializes) a configuration file from some source location
     *
     * @param source the source representing the VLO configuration to unmarshal
     * @return the VLO configuration as described by the source
     * @throws JAXBException if an error occurs while unmarshalling
     */
    public final VloConfig unmarshal(Source source) throws JAXBException {
        final Unmarshaller unmarshaller = jc.createUnmarshaller();
        SchemaFactory xsdFac = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // custom parser, so that we can make it XInclude aware
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);

        try {
            // to prevent the parser adding xml:base
            spf.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);

            Schema schema = xsdFac.newSchema(getClass().getResource("/VloConfig.xsd"));
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(getConfigValidationEventHandler());

            final XMLReader xr = spf.newSAXParser().getXMLReader();
            // XML transformation 'source' needs to be converted to a SAX 'input source'
            final InputSource inputSource = SAXSource.sourceToInputSource(source);
            final SAXSource saxSource = new SAXSource(xr, inputSource);

            return (VloConfig) unmarshaller.unmarshal(saxSource);
        } catch (ParserConfigurationException | SAXException ex) {
            throw new JAXBException(ex);
        }
    }

    protected ValidationEventHandler getConfigValidationEventHandler() {
        return validationEvent -> {
            _logger.warn(validationEvent.toString());
            return true; // unmarshalling should continue in case of validation error
        };
    }

}
