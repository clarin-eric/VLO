/*
 * Copyright (C) 2015 Max Planck Institute for Psycholinguistics
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

import com.google.common.collect.ImmutableMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import nl.mpi.archiving.corpusstructure.core.URLConnections;

/**
 * Resolve a handle into an URI, tries to use the local handle resolver for know
 * prefixes
 *
 * TODO: make prefix, resolver pairs configurable
 *
 * @author wilelb
 */
public class OptimizedHttpHandleResolver extends HttpHandleResolver {

    /**
     * Map from prefix to handle server
     */
    private static final Map<String, HandleServer> HANDLE_SERVERS = ImmutableMap.<String, HandleServer>builder()
            .put("1839", new HandleServer("lux08.mpi.nl", 8000)) // MPI production
            .put("11142", new HandleServer("lux17.mpi.nl", 8000)) // MPI test
            .put("LUX16", new HandleServer("lux16.mpi.nl", 8000)) // MPI dummy
            .build();

    /**
     * Constructor for a resolver that follows one redirect
     */
    public OptimizedHttpHandleResolver() {
        super();
    }

    /**
     * Constructor that uses the provided connection utility
     *
     * @param connections instance to use for following HTTP redirects
     */
    public OptimizedHttpHandleResolver(URLConnections connections) {
        super(connections);
    }

    /**
     *
     */
    @Override
    protected URL determineResolveUrl(URL url) throws MalformedURLException {
        if (url.getHost() != null && url.getHost().equalsIgnoreCase("hdl.handle.net")) {
            final HandleServer server = getHandleServer(url);
            return new URL(url.getProtocol(), server.host, server.port, url.getFile());
        }
        return url;
    }

    private HandleServer getHandleServer(URL url) {
        String urlPath = url.getPath();
        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }
        if (urlPath.startsWith("hdl:")) {
            urlPath = urlPath.substring(4);
        }

        final int prefixEnd = urlPath.indexOf("/");
        if (prefixEnd >= 0) {
            // skip URLs without prefix...
            final String prefix = urlPath.substring(0, prefixEnd);
            final HandleServer server = HANDLE_SERVERS.get(prefix);
            if (server != null) {
                return server;
            }
        }
        return new HandleServer(url.getHost(), url.getPort());
    }

    private static class HandleServer {

        public HandleServer(String host, int port) {
            this.host = host;
            this.port = port;
        }

        String host;
        int port;
    }
}
