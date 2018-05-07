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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.service.XmlTransformationService;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms input documents to a fixed stylesheet (determined on construction)
 * using JAXP XSLT support.
 *
 * Thread-safe
 *
 * @author twagoo
 */
public class XmlTransformationServiceImpl implements XmlTransformationService {

    private final static Logger logger = LoggerFactory.getLogger(XmlTransformationServiceImpl.class);

    private Future<Templates> templatesFuture;
    private final Properties transformationProperties;
    private final Source xsltSource;

    /**
     *
     * @param xsltSource source of the stylesheet to use in {@link #transformXml(java.net.URL)
     * }
     * @param properties transformation properties to be passed to the
     * transformer object before transformation
     *
     */
    public XmlTransformationServiceImpl(Source xsltSource, Properties properties) {
        this.xsltSource = xsltSource;
        this.transformationProperties = properties;
    }

    /**
     * Make transformation templates from xslt source
     *
     * @throws RuntimeException if the transformer could not be configured
     * correctly
     */
    @PostConstruct
    public void init() {
        logger.debug("Initializing transformation service with {} and properties {}", xsltSource.getSystemId(), transformationProperties);
        
        //initialise in separate thread as this can take a while and could slow down application startup
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        this.templatesFuture = executor.submit(() -> {
            return compileTemplates();
        });
    }

    private Templates compileTemplates() throws TransformerConfigurationException {
        logger.debug("Compiling stylesheet {} with properties {}", xsltSource.getSystemId(), transformationProperties);
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // create a template to derive transformers from, which makes this class thread safe
        final Templates template = transformerFactory.newTemplates(xsltSource);

        logger.debug("Done compiling stylesheet {}", xsltSource.getSystemId());        
        return template;
    }

    /**
     *
     * @param location location of XML document to transform (should not be
     * null)
     * @return the result of the XML transformation as a string
     * @throws TransformerException If an unrecoverable error occurs during the
     * course of the transformation or while opening the input stream.
     */
    @Override
    public String transformXml(URL location) throws TransformerException {
        logger.debug("Transforming {}", location);

        try {
            // create a transformer based on the template
            final Transformer transformer = templatesFuture.get().newTransformer();
            transformer.setOutputProperties(transformationProperties);

            final InputStream inStream = location.openStream();
            final StringWriter outWriter = new StringWriter();

            // make input/output objects
            final Source source = new StreamSource(inStream);
            final Result result = new StreamResult(outWriter);

            // perform actual transformation (this will also close the streams)
            transformer.transform(source, result);

            // result has been written to string writer, return contents
            return outWriter.toString();
        } catch (IOException ex) {
            throw new TransformerException("Error while opening input stream for " + location.toString(), ex);
        } catch (ExecutionException ex) {
            throw new TransformerException("Compilation of stylesheet " + location.toString() + " errored", ex);
        } catch (InterruptedException ex) {
            throw new TransformerException("Thread compiling stylesheet " + location.toString() + " was interrupted", ex);
        }

    }

}
