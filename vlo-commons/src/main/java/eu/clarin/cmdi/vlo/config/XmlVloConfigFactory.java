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
import java.net.URL;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author twagoo
 */
public class XmlVloConfigFactory implements VloConfigFactory {

    private final VloConfigMarshaller marshaller;
    private final URL configLocation;

    public XmlVloConfigFactory(URL configLocation) {
        this.configLocation = configLocation;
        try {
            this.marshaller = new VloConfigMarshaller();
        } catch (JAXBException ex) {
            throw new RuntimeException("Could not instantiate configuration marshaller while constructing configuration factory", ex);
        }
    }

    public VloConfig newConfig() {
        try {
            final InputStream fileStream = configLocation.openStream();
            try {
                return marshaller.unmarshal(new StreamSource(fileStream));
            } catch (JAXBException ex) {
                throw new RuntimeException("Could not deserialize configuration file", ex);
            } finally {
                fileStream.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not read configuration file", ex);
        }
    }
}
