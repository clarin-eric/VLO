/*
 * Copyright (C) 2018 CLARIN
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

import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public interface FacetProcessor {

    /**
     * Extracts facet values according to the facetMapping
     *
     * @param cmdiData representation of the CMDI document
     * @param facetMapping the facet mapping used to map meta data to facets
     * @throws eu.clarin.cmdi.vlo.importer.processor.CMDIParsingException
     * @throws java.net.URISyntaxException
     * @throws java.io.UnsupportedEncodingException
     */
    void processFacets(CMDIData cmdiData, FacetMapping facetMapping) throws CMDIParsingException, URISyntaxException, UnsupportedEncodingException;

}
