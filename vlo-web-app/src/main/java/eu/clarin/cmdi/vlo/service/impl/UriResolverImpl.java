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
import eu.clarin.cmdi.vlo.service.UriResolver;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.archiving.corpusstructure.core.handle.HandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves a URI as follows: if the URI starts with the handle scheme or the
 * handle proxy, the handle is extracted and passed on to this resolver's
 * {@link HandleResolver} and the result of {@link HandleResolver#resolve(java.net.URI)
 * } is returned (as String); otherwise the original URI is returned.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class UriResolverImpl implements UriResolver {

    private final static Logger logger = LoggerFactory.getLogger(UriResolverImpl.class);

    private final HandleResolver handleClient;
    private final HandleResolver doiClient;

    public UriResolverImpl(HandleResolver handleClient, HandleResolver doiClient) {
        this.handleClient = handleClient;
        this.doiClient = doiClient;
    }

    @Override
    public String resolve(String uri) {
        final URI resolved;
        if (PIDUtils.isHandle(uri)) {
            resolved = resolve(handleClient, HANDLE_PREFIX + PIDUtils.getSchemeSpecificId(uri));
        } else if (PIDUtils.isDoi(uri)) {
            resolved = resolve(doiClient, DOI_PREFIX + PIDUtils.getSchemeSpecificId(uri));
        } else {
            resolved = null;
        }

        return (resolved != null) ? resolved.toString() : uri;
    }

    private final static URI resolve(HandleResolver resolver, String pid) {
        try {
            logger.debug("Using {} to resolve pid [{}]", resolver.getClass().getName(), pid);
            return resolver.resolve(new URI(pid));
        } catch (InvalidHandleException ex) {
            logger.warn("Invalid PID ecountered (resolver {}): {}", resolver.getClass().getName(), pid);
            return null;
        } catch (URISyntaxException ex) {
            logger.warn("PID is not a valid URI: {}", pid);
            return null;
        }
    }

}
