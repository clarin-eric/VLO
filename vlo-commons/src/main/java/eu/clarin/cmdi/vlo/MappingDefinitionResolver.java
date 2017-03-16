/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class MappingDefinitionResolver {

    private final static Logger LOG = LoggerFactory.getLogger(MappingDefinitionResolver.class);

    private final Class resourceContextClass;

    public MappingDefinitionResolver(Class resourceContextClass) {
        this.resourceContextClass = resourceContextClass;
    }

    public InputStream tryResolveUrlFileOrResourceStream(String mapUrl) throws IOException {
        InputStream stream;
        //first try as absolute URL
        final InputStream urlStream = getUrlStream(mapUrl);
        if (urlStream != null) {
            stream = urlStream;
        } else {
            //not an absolute URL try absolute file path
            final InputStream fileStream = getFileStream(mapUrl);
            if (fileStream != null) {
                stream = fileStream;
            } else {
                //not an absolute file path - try resource
                stream = getResourceStream(mapUrl);
            }
        }
        return stream;
    }

    private InputStream getUrlStream(String potentialUrl) throws IOException {
        LOG.trace("Looking for URL {}", potentialUrl);
        try {
            final URL url = new URL(potentialUrl);
            if (url.toURI().isAbsolute()) {
                return url.openStream();
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            LOG.debug("Not a valid vocabulary URL / URI: {}", potentialUrl);
        }
        //conditions not met - not a valid absolute URL
        return null;
    }

    private InputStream getFileStream(String potentialPath) {
        LOG.trace("Looking for local path {}", potentialPath);
        final File file = new File(potentialPath);
        try {
            if (file.isAbsolute()) {
                return new FileInputStream(file);
            } else {
                LOG.debug("Not an absolute file path: {}", potentialPath);
            }
        } catch (FileNotFoundException ex) {
            LOG.debug("Not a local file that exists: {}", potentialPath);
        }
        //conditions not met - not a valid absolute path
        return null;
    }

    private InputStream getResourceStream(String resourceName) {
        LOG.trace("Looking for bundled resource {}", resourceName);
        return resourceContextClass.getResourceAsStream(resourceName);
    }
}
