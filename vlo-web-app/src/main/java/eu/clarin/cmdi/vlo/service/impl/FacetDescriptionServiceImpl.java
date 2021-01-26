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
import eu.clarin.cmdi.vlo.MappingDefinitionResolver;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.facets.FacetsConfigurationsMarshaller;
import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.service.FacetDescriptionService;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FacetDescriptionServiceImpl implements FacetDescriptionService {

    private final static Logger logger = LoggerFactory.getLogger(FacetDescriptionServiceImpl.class);

    private final Map<String, Facet> facets = new CopyOnWriteHashMap<>();
    private final VloConfig config;
    private final FacetsConfigurationsMarshaller marshaller;
    private final MappingDefinitionResolver mappingDefinitionResolver = new MappingDefinitionResolver(FacetDescriptionServiceImpl.class);

    public FacetDescriptionServiceImpl(FacetsConfigurationsMarshaller marshaller, VloConfig vloConfig) {
        this.marshaller = marshaller;
        this.config = vloConfig;
    }

    @PostConstruct
    protected void init() {
        try {
            final Source streamSource = getFacetsConfigSource();
            final FacetsConfiguration facetsConfiguration = marshaller.unmarshal(streamSource);
            for (Facet facet : facetsConfiguration.getFacet()) {
                if (facet.getDescription() != null) {
                    logger.debug("Found facet configuration '{}'", facet.getName());
                    facets.put(facet.getName(), facet);
                }
            }
        } catch (JAXBException | IOException ex) {
            throw new RuntimeException("Failed to initialise the facet description provider!", ex);
        }
    }

    private Source getFacetsConfigSource() throws IOException {
        final String facetConceptsFile = config.getFacetsConfigFile();

        if (Strings.isNullOrEmpty(facetConceptsFile)) {
            logger.info("No facet concepts file configured. Reading default definitions from packaged file.");
            return new StreamSource(getClass().getResourceAsStream(VloConfig.DEFAULT_FACETS_CONFIG_RESOURCE_FILE));
        } else {
            final InputSource stream = mappingDefinitionResolver.tryResolveUrlFileOrResourceStream(facetConceptsFile);
            if (stream != null) {
                logger.info("Reading facet definitions from {}", facetConceptsFile);
                return new StreamSource(stream.getByteStream(), stream.getSystemId());
            } else {
                return null;
            }
        }
    }

    @Override
    public String getDescription(String facetName) {
        return Optional.ofNullable(facets.get(facetName))
                .map(Facet::getDescription)
                .orElse(null);
    }

}
