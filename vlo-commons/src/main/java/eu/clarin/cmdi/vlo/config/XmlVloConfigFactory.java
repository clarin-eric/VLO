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
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Factory that reads a VLO configuration from a URL (typically a local file)
 *
 * @author twagoo
 */
public class XmlVloConfigFactory extends AbstractXmlVloConfigFactory implements VloConfigFactory {

    private final URL configLocation;

    /**
     *
     * @param configLocation URL from which the configuration should be read
     */
    public XmlVloConfigFactory(URL configLocation) {
        this.configLocation = configLocation;
    }

    protected InputStream getXmlConfigurationInputStream() throws IOException {
        return configLocation.openStream();
    }

    @Override
    protected URI getLocation() {
        try {
            return configLocation.toURI();
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Invalid config file URI", ex);
        }
    }

}
