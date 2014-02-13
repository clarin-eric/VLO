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

import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author twagoo
 */
public class DefaultVloConfigFactory implements VloConfigFactory {

    public static final String DEFAULT_CONFIG_RESOURCE = "/VloConfig.xml";
    private final VloConfigMarshaller marshaller;

    public DefaultVloConfigFactory() {
        try {
            this.marshaller = new VloConfigMarshaller();
        } catch (JAXBException ex) {
            throw new RuntimeException("Could not instantiate configuration marshaller while constructing configuration factory", ex);
        }
    }

    public VloConfig newConfig() {
        InputStream configResourceStream = getClass().getResourceAsStream(DEFAULT_CONFIG_RESOURCE);
        try {
            return marshaller.unmarshal(new StreamSource(configResourceStream));
        } catch (JAXBException ex) {
            throw new RuntimeException("Could not read default configuration due to deserialization error", ex);
        }
    }
}
