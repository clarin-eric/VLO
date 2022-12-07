/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.archiving.corpusstructure.core.handle;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import nl.mpi.archiving.corpusstructure.core.URLConnections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class HttpHandleResolver implements HandleResolver, Serializable {

    private final static Logger logger = LoggerFactory.getLogger(HttpHandleResolver.class);
    private final URLConnections connections;

    /**
     * Constructor for a resolver that follows one redirect
     */
    public HttpHandleResolver() {
        this(new URLConnections());
    }

    /**
     * Constructor that uses the provided connection utility
     *
     * @param connections instance to use for following HTTP redirects
     */
    public HttpHandleResolver(URLConnections connections) {
        this.connections = connections;
    }

    /**
     * Resolve a handle into an URI using the global handle resolver
     *
     * @param uri
     * @return
     * @throws InvalidHandleException
     */
    @Override
    public URI resolve(URI uri) throws InvalidHandleException {
        try {
            if (uri.getScheme() == null) {
                throw new InvalidHandleException("Scheme is required");
            }
            if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
                return resolve(uri.toURL()).toURI();
            } else if (uri.getScheme().equalsIgnoreCase("hdl")) {
                return resolve(new URL("http://hdl.handle.net/" + uri.getSchemeSpecificPart())).toURI();
            } else {
                throw new InvalidHandleException("Handle uri scheme [" + uri.getScheme() + "] is invalid.");
            }
        } catch (IOException ex) {
            throw new InvalidHandleException("Handle resolution error", ex);
        } catch (URISyntaxException ex) {
            throw new InvalidHandleException("Handle resolution error", ex);
        }
    }

    protected URL resolve(URL url) throws IOException {
        logger.debug("Resolving handle URL {}", url);
        HttpURLConnection httpCon = null;
        try {
            final URL resolveUrl = determineResolveUrl(url);
            logger.debug("connecting to [{}]", resolveUrl);

            if (resolveUrl.getProtocol().equalsIgnoreCase("file")) {
                return resolveUrl;
            } else {
                httpCon = (HttpURLConnection) resolveUrl.openConnection();
                httpCon.setInstanceFollowRedirects(true);
                return connections.getUrlCheckRedirects(httpCon);
            }
        } finally {
            if (httpCon != null) {
                httpCon.disconnect();
            }
        }
    }

    protected URL determineResolveUrl(URL url) throws MalformedURLException {
        return url;
    }
}
