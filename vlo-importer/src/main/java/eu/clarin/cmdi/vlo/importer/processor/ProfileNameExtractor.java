/*
 * Copyright (C) 2020 CLARIN
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
package eu.clarin.cmdi.vlo.importer.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileNameExtractor {
    private final static Logger LOG = LoggerFactory.getLogger(ProfileNameExtractor.class);

    private final String registryBaseURl;
    private final ConcurrentMap<String, String> profileNameCache = new ConcurrentHashMap<>();

    public ProfileNameExtractor(VloConfig config) {
        registryBaseURl = config.getComponentRegistryRESTURL();
    }

    /**
     * Get profile name for profileID
     * @param profileId
     * @return profile name
     */
    public String process(String profileId) {
        if (profileId != null) {
            return profileNameCache.computeIfAbsent(profileId, (key) -> {
                try {
                    return calculate(key);
                } catch (IOException ex) {
                    LOG.error("Error while looking up profile name for profile {}", profileId, ex);
                    return null;
                }
            });
        } else {
            return null;
        }
    }

    /**
     * Gets the name of the profile from the expanded xml in the component
     * registry
     *
     * @param profileId
     * @return
     * @throws VTDException
     */
    private String calculate(String profileId) throws IOException {
        LOG.debug("PARSING PROFILE: {}{}", registryBaseURl, profileId);

        try ( InputStream is = getInputStream(profileId)) {
            if (is == null) {
                throw new IOException("No input stream");
            }
            final String profileName = extractProfileName(is);
            if (profileName == null) {
                LOG.error("Cannot open and/or parse XML Schema: {}.", registryBaseURl + profileId);
                throw new IOException("Cannot open and/or parse XML Schema");
            }
            LOG.debug("PARSED PROFILE: {}{}", registryBaseURl, profileId);
            return profileName;
        }
    }

    protected InputStream getInputStream(String profileId) throws IOException {
        final String profileUrl = registryBaseURl + profileId + "/xml";
        LOG.debug("Opening input stream at {}", profileUrl);
        return new URL(profileUrl).openStream();
    }

    /**
     * * Target path in XML document: /ComponentSpec/Header/Name/text()
     */
    private final static List<QName> TARGET_PATH = ImmutableList.of(new QName("ComponentSpec"), new QName("Header"), new QName("Name")).reverse();
    /**
     * * Halting path. No component name link past Header section...
     */
    private final static List<QName> HALT_PATH = ImmutableList.of(new QName("ComponentSpec"), new QName("Component")).reverse();
    private final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

    private String extractProfileName(InputStream fileInputStream) throws IOException {
        try {
            final LinkedList<QName> stack = new LinkedList<>();
            final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(fileInputStream);
            while (reader.hasNext()) {
                reader.next();
                if (reader.isStartElement()) {
                    stack.push(reader.getName());
                    if (isHaltPath(stack)) {
                        LOG.debug("Halt point reached, no self link found in document");
                        return null;
                    }
                } else if (reader.isEndElement()) {
                    stack.pop();
                } else if (reader.isCharacters()) {
                    if (isTargetPath(stack)) {
                        LOG.trace("Profile name found in document");
                        return reader.getText();
                    }
                }
            }
        } catch (XMLStreamException ex) {
            LOG.error("Error while parsing XML to find profile name");
        }
        LOG.debug("Document processing completed, failed to find profile name in {}");
        return null;
    }

    private static boolean isTargetPath(final LinkedList<QName> stack) {
        return Iterables.elementsEqual(TARGET_PATH, stack);
    }

    private static boolean isHaltPath(final LinkedList<QName> stack) {
        return Iterables.elementsEqual(HALT_PATH, stack);
    }
}
