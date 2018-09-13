package eu.clarin.cmdi.vlo.importer.normalizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.ximpleware.VTDException;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.Collections;
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

/**
 * Takes the value of the componentprofileid and uses the componentregistry REST
 * service to transform this to the name of the componentprofile.
 */
public class CMDIComponentProfileNamePostNormalizer extends AbstractPostNormalizer {

    private final String registryBaseURl;

    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostNormalizer.class);
    private final ConcurrentMap<String, List<String>> cache = new ConcurrentHashMap<>();
    private final ProfileNameExtractor nameExtractor = new ProfileNameExtractor();

    public CMDIComponentProfileNamePostNormalizer(VloConfig config) {
        super(config);
        registryBaseURl = config.getComponentRegistryRESTURL();
    }

    @Override
    public List<String> process(String profileId, DocFieldContainer cmdiData) {
        if (profileId != null) {
            return cache.computeIfAbsent(profileId, (key) -> {
                try {
                    return Collections.singletonList(calculate(key));
                } catch (IOException ex) {
                    LOG.error("Error while looking up profile name for profile {}", profileId, ex);
                    return null;
                }
            });
        } else {
            return Collections.singletonList("");
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

        try (InputStream is = getInputStream(profileId)) {
            if (is == null) {
                throw new IOException("No input stream");
            }
            final String profileName = nameExtractor.extractProfileName(is);
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

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

    public static class ProfileNameExtractor {

        /**
         * Target path in XML document: /ComponentSpec/Header/Name/text()
         */
        private final static List<QName> TARGET_PATH = ImmutableList.of(
                new QName("ComponentSpec"),
                new QName("Header"),
                new QName("Name")
        ).reverse();

        /**
         * Halting path. No component name link past Header section...
         */
        private final static List<QName> HALT_PATH = ImmutableList.of(
                new QName("ComponentSpec"),
                new QName("Component")
        ).reverse();

        private final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

        public String extractProfileName(InputStream fileInputStream) throws IOException {
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
}
