/*
 * Copyright (C) 2014 CLARIN
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

import eu.clarin.cmdi.vlo.service.handle.impl.HandleRestApiClient;
import eu.clarin.cmdi.vlo.PIDUtils;
import eu.clarin.cmdi.vlo.service.handle.HandleClient;
import eu.clarin.cmdi.vlo.service.UriResolver;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves a URI as follows: if the URI starts with the handle scheme or the
 * handle proxy, the handle is extracted and passed on to this resolver's
 * {@link HandleClient} and the result of {@link HandleClient#getUrl(java.lang.String)
 * } is returned; otherwise the original URI is returned.
 *
 * TODO: add support for resolving URN:NBN <https://trac.clarin.eu/ticket/535>
 *
 * @author twagoo
 */
public class HandleClientUriResolverImpl implements UriResolver {

    private final static Logger logger = LoggerFactory.getLogger(HandleRestApiClient.class);

    private final HandleClient handleClient;

    public HandleClientUriResolverImpl(HandleClient handleClient) {
        this.handleClient = handleClient;
    }

    @Override
    public boolean canResolve(String uri) {
        return PIDUtils.isHandle(uri);
    }

    @Override
    public Optional<String> resolve(String uri) {
        final String handle = getHandle(uri);

        if (handle == null) {
            return Optional.empty();
        } else {
            logger.debug("Calling handle client to resolve handle [{}]", uri);
            final String resolved = handleClient.getUrl(handle);
            return Optional.ofNullable(resolved);
        }

    }

    private String getHandle(String uri) {
        if (PIDUtils.isHandle(uri)) {
            return PIDUtils.getSchemeSpecificId(uri);
        } else {
            return null;
        }
    }

}
