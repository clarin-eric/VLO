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

import static eu.clarin.cmdi.vlo.FacetConstants.DOI_PREFIX;
import static eu.clarin.cmdi.vlo.FacetConstants.HANDLE_PREFIX;
import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.service.PIDResolver;
import eu.clarin.cmdi.vlo.service.UriResolver;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves a URI as follows: if the URI is a recognised handle or DOI, the PID
 * is extracted and passed on to this resolver's {@link PIDResolver} and the
 * result of {@link PIDResolver#resolve(java.net.URI)} is returned (as String);
 * otherwise the original URI is returned.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class UriResolverImpl implements UriResolver {

    private final static Logger logger = LoggerFactory.getLogger(UriResolverImpl.class);

    private final PIDResolver handleClient;
    private final PIDResolver doiClient;

    public UriResolverImpl(PIDResolver handleClient, PIDResolver doiClient) {
        this.handleClient = handleClient;
        this.doiClient = doiClient;
    }

    @Override
    public boolean canResolve(String uri) {
        return PIDUtils.isHandle(uri) || PIDUtils.isDoi(uri);
    }

    @Override
    public Optional<String> resolve(String uri) {
        if (PIDUtils.isHandle(uri)) {
            return resolve(handleClient, HANDLE_PREFIX + PIDUtils.getSchemeSpecificId(uri)).map(Object::toString);
        } else if (PIDUtils.isDoi(uri)) {
            return resolve(doiClient, DOI_PREFIX + PIDUtils.getSchemeSpecificId(uri)).map(Object::toString);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<URI> resolve(PIDResolver resolver, String pid) {
        try {
            logger.debug("Using {} to resolve pid [{}]", resolver.getClass().getName(), pid);
            return Optional.ofNullable(resolver.resolve(new URI(pid)));
        } catch (URISyntaxException ex) {
            logger.warn("PID is not a valid URI: {}", pid);
            return Optional.empty();
        }
    }

}
