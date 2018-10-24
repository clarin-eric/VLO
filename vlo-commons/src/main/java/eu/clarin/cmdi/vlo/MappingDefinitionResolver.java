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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

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
    
    public InputSource tryResolveUrlFileOrResourceStream(String mapUrl) throws IOException {
        //first try as absolute URL
        final InputSource urlStreamSource = getUrlStream(mapUrl);
        if (urlStreamSource != null) {
            return urlStreamSource;
        } else {
            //not an absolute URL try absolute file path
            final InputSource fileStreamSource = getFileStream(mapUrl);
            if (fileStreamSource != null) {
                return fileStreamSource;
            } else {
                //not an absolute file path - try resource
                return getResourceStream(mapUrl);
            }
        }
    }
    
    private InputSource getUrlStream(String potentialUrl) throws IOException {
        LOG.trace("Looking for URL {}", potentialUrl);
        try {
            final URL url = new URL(potentialUrl);
            if (url.toURI().isAbsolute()) {
                final InputSource inputSource = new InputSource(url.openStream());
                inputSource.setSystemId(url.toString());
                return inputSource;
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            LOG.debug("Not a valid vocabulary URL / URI: {}", potentialUrl);
        }
        //conditions not met - not a valid absolute URL
        return null;
    }
    
    private InputSource getFileStream(String potentialPath) {
        LOG.trace("Looking for local path {}", potentialPath);
        final File file = new File(potentialPath);
        try {
            if (file.isAbsolute()) {
                final InputSource inputSource = new InputSource(new FileInputStream(file));
                inputSource.setSystemId(file.toURI().toString());
                return inputSource;
            } else {
                LOG.debug("Not an absolute file path: {}", potentialPath);
            }
        } catch (FileNotFoundException ex) {
            LOG.debug("Not a local file that exists: {}", potentialPath);
        }
        //conditions not met - not a valid absolute path
        return null;
    }
    
    private InputSource getResourceStream(String resourceName) {
        LOG.trace("Looking for bundled resource {}", resourceName);
        return new InputSource(resourceContextClass.getResourceAsStream(resourceName));
    }
}
