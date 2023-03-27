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
import java.util.Objects;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author twagoo
 */
public class MappingDefinitionResolver {

    private final static Logger LOG = LoggerFactory.getLogger(MappingDefinitionResolver.class);

    private final Class resourceContextClass;

    private enum Type {
        URL,
        FILE,
        BUNDLED_RESOURCE
    }

    public MappingDefinitionResolver(Class resourceContextClass) {
        this.resourceContextClass = resourceContextClass;
    }

    public Document tryParse(String identifier) throws IOException, SAXException, ParserConfigurationException {
        final ResolutionInfo resolutionInfo = getResolutionInfo(identifier);
        if (resolutionInfo == null) {
            throw new IOException("No input source could be determined for " + identifier);
        }
        final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setXIncludeAware(true);
        fac.setNamespaceAware(true);
        final DocumentBuilder builder = fac.newDocumentBuilder();

        // in case of a bundled resource, we need to configure a custom resolver
        // to make sure that referenced entites can also be resolved
        if (resolutionInfo.getType() == Type.BUNDLED_RESOURCE) {
            builder.setEntityResolver(new MappingDefinitionEntityResolver());
        }

        return builder.parse(resolutionInfo.getInputSource());
    }

    public InputSource tryResolveUrlFileOrResourceStream(String identifier) throws IOException {
        return Optional.ofNullable(
                getResolutionInfo(identifier))
                .map(ResolutionInfo::getInputSource)
                .orElse(null);
    }

    private ResolutionInfo getResolutionInfo(String identifier) throws IOException {
        //first try as absolute URL
        final ResolutionInfo urlStreamSource = getUrlStream(identifier);
        if (urlStreamSource != null) {
            return urlStreamSource;
        } else {
            //not an absolute URL try absolute file path
            final ResolutionInfo fileStreamSource = getFileStream(identifier);
            if (fileStreamSource != null) {
                return fileStreamSource;
            } else {
                //not an absolute file path - try resource
                return getResourceStream(identifier);
            }
        }
    }

    private ResolutionInfo getUrlStream(String potentialUrl) throws IOException {
        LOG.trace("Looking for URL {}", potentialUrl);
        try {
            final URL url = new URL(potentialUrl);
            if (url.toURI().isAbsolute()) {
                final InputSource inputSource = new InputSource(url.openStream());
                inputSource.setSystemId(url.toString());
                return new ResolutionInfo(Type.URL, inputSource);
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            LOG.debug("Not a valid vocabulary URL / URI: {}", potentialUrl);
        }
        //conditions not met - not a valid absolute URL
        return null;
    }

    private ResolutionInfo getFileStream(String potentialPath) {
        LOG.trace("Looking for local path {}", potentialPath);
        final File file = new File(potentialPath);
        try {
            if (file.isAbsolute()) {
                final InputSource inputSource = new InputSource(new FileInputStream(file));
                inputSource.setSystemId(file.toURI().toString());
                return new ResolutionInfo(Type.FILE, inputSource);
            } else {
                LOG.debug("Not an absolute file path: {}", potentialPath);
            }
        } catch (FileNotFoundException ex) {
            LOG.debug("Not a local file that exists: {}", potentialPath);
        }
        //conditions not met - not a valid absolute path
        return null;
    }

    private ResolutionInfo getResourceStream(String resourceName) {
        LOG.trace("Looking for bundled resource {}", resourceName);
        final InputSource inputSource = new InputSource(resourceContextClass.getResourceAsStream(resourceName));
        inputSource.setSystemId(resourceContextClass.getResource(resourceName).toString());
        return new ResolutionInfo(Type.BUNDLED_RESOURCE, inputSource);
    }

    private static class ResolutionInfo {

        private final Type type;
        private final InputSource inputSource;

        public ResolutionInfo(Type type, InputSource inputSource) {
            this.type = type;
            this.inputSource = inputSource;
        }

        public Type getType() {
            return type;
        }

        public InputSource getInputSource() {
            return inputSource;
        }

    }

    /**
     * Entity resolver that uses {@link MappingDefinitionResolver}
     */
    private class MappingDefinitionEntityResolver implements EntityResolver {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            LOG.debug("Using mapping definition resolver to resolve entity with systemId: {}", systemId);
            if (systemId == null) {
                throw new IOException("SystemId null (publicId: " + Objects.toString(publicId) + ")");
            }
            final ResolutionInfo resolutionInfo1 = getResolutionInfo(systemId);
            if (resolutionInfo1 == null) {
                throw new IOException("No input source could be determined for " + systemId);
            }
            return resolutionInfo1.getInputSource();
        }
    }

}
