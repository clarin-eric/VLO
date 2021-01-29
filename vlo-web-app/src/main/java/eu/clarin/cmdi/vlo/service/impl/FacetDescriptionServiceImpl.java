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

import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.facets.configuration.FacetsConfiguration;
import eu.clarin.cmdi.vlo.service.FacetDescriptionService;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class FacetDescriptionServiceImpl implements FacetDescriptionService {

    private final static Logger logger = LoggerFactory.getLogger(FacetDescriptionServiceImpl.class);

    private final Map<String, Facet> facets = new CopyOnWriteHashMap<>();
    private final FacetsConfiguration facetsConfiguration;

    public FacetDescriptionServiceImpl(FacetsConfiguration facetsConfiguration) {
        this.facetsConfiguration = facetsConfiguration;
    }

    @PostConstruct
    protected void init() {
        for (Facet facet : facetsConfiguration.getFacet()) {
            if (facet.getDescription() != null) {
                logger.debug("Found facet configuration '{}'", facet.getName());
                facets.put(facet.getName(), facet);
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
