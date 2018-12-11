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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.service.PIDResolver;
import eu.clarin.cmdi.vlo.service.handle.impl.HandleRestApiClient;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves DOIs via the DOI handle API
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see HandleRestApiClient
 */
public class DOIResolver implements PIDResolver {

    private final static Logger logger = LoggerFactory.getLogger(DOIResolver.class);

    private final static String DOI_API_URL = "https://doi.org/api/handles/";
    private final HandleRestApiClient handleApiClient;

    public DOIResolver() {
        handleApiClient = new HandleRestApiClient(DOI_API_URL);
    }

    @Override
    public URI resolve(URI uri) {
        final String doi = PIDUtils.getSchemeSpecificId(uri.toString());
        if (doi == null) {
            logger.info("DOI not found in URI: {}", uri);
            return null;
        } else {
            return resolveDoi(doi);
        }
    }

    private URI resolveDoi(final String doi) {
        final String resolved = handleApiClient.getUrl(doi);
        if (resolved == null) {
            logger.warn("DOI was not resolved through client: {}", doi);
            return null;
        } else {
            return toUri(resolved);
        }
    }

    private URI toUri(final String resolved) {
        try {
            return new URI(resolved);
        } catch (URISyntaxException ex) {
            logger.warn("Returned DOI is not a valid URI: {}", resolved);
            return null;
        }
    }

}
