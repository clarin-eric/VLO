/*
 * Copyright (C) 2015 CLARIN
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

import com.google.common.base.Strings;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.facets.FacetConcept;
import eu.clarin.cmdi.vlo.facets.FacetConcepts;
import eu.clarin.cmdi.vlo.facets.FacetConceptsMarshaller;
import eu.clarin.cmdi.vlo.service.FacetDescriptionService;
import java.io.File;
import java.net.URI;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FacetDescriptionServiceImpl implements FacetDescriptionService {

    private final static Logger logger = LoggerFactory.getLogger(FacetDescriptionServiceImpl.class);

    private final Map<String, String> descriptions = new CopyOnWriteHashMap<>();
    private final VloConfig config;
    private final FacetConceptsMarshaller marshaller;

    public FacetDescriptionServiceImpl(FacetConceptsMarshaller marshaller, VloConfig vloConfig) {
        this.marshaller = marshaller;
        this.config = vloConfig;
    }

    @PostConstruct
    protected void init() throws JAXBException {
        final Source streamSource = getFacetConceptsSource();
        final FacetConcepts facetConcepts = marshaller.unmarshal(streamSource);
        for (FacetConcept concept : facetConcepts.getFacetConcept()) {
            if (concept.getDescription() != null) {
                logger.debug("Found facet definition '{}'", concept.getName());
                descriptions.put(concept.getName(), concept.getDescription());
            }
        }
    }

    private Source getFacetConceptsSource() {
        final String facetConceptsFile = config.getFacetConceptsFile();

        if (Strings.isNullOrEmpty(facetConceptsFile)) {
            logger.info("No facet concepts file configured. Reading default definitions from packaged file.");
            return new StreamSource(getClass().getResourceAsStream(VloConfig.DEFAULT_FACET_CONCEPTS_RESOURCE_FILE));
        } else {
            final URI facetsFile = resolveFacetsFile(facetConceptsFile);
            logger.info("Reading facet definitions from {}", facetsFile);
            return new StreamSource(facetsFile.toString());
        }
    }

    private URI resolveFacetsFile(final String facetConceptsFile) {
        final URI configLocation = config.getConfigLocation();
        if (configLocation == null) {
            return new File(facetConceptsFile).toURI();
        } else if ("jar".equals(configLocation.getScheme())) {
            // some trickery to replace URI pointing inside JAR (fingers crossed)
            final String jarUri = configLocation.toString().replaceAll("!.*$", "!" + facetConceptsFile);
            return URI.create(jarUri);
        } else {
            // resolve against config
            return configLocation.resolve(facetConceptsFile);
        }
    }

    @Override
    public String getDescription(String facetName) {
        return descriptions.get(facetName);
    }

}
