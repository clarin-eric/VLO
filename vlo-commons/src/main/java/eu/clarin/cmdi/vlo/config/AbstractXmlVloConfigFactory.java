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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public abstract class AbstractXmlVloConfigFactory implements VloConfigFactory {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractXmlVloConfigFactory.class);
    private final VloConfigMarshaller marshaller;

    public AbstractXmlVloConfigFactory() {
        try {
            this.marshaller = new VloConfigMarshaller();
        } catch (JAXBException ex) {
            throw new RuntimeException("Could not instantiate configuration marshaller while constructing configuration factory", ex);
        }
    }

    public VloConfig newConfig() throws IOException {
        final InputStream fileStream = getXmlConfigurationInputStream();
        try {
            final URI location = getLocation();
            LOG.debug("Config location: {}", location);
            
            final VloConfig config = marshaller.unmarshal(new StreamSource(fileStream, location.toString()));
            config.setConfigLocation(location);
            
            return config;
        } catch (JAXBException ex) {
            throw new RuntimeException("Could not deserialize configuration file", ex);
        } finally {
            fileStream.close();
        }
    }

    /**
     *
     * @return an input stream to the XML representation of a VLO configuration
     *
     * @throws IOException if stream could not be opened or read
     */
    protected abstract InputStream getXmlConfigurationInputStream() throws IOException;

    protected abstract URI getLocation();

}
