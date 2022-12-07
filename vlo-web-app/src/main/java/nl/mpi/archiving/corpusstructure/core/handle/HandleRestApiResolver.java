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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HandleRestApiResolver implements HandleResolver {

    private static final Logger logger = LoggerFactory.getLogger(HandleRestApiResolver.class);
    private static final String HANDLE_PREFIX = "hdl:";
    private static final String HANDLE_PROXY = "http://hdl.handle.net/";
    private static final String DEFAULT_HANDLE_API_URL = "http://hdl.handle.net/api/handles/";

    private final String handleApiBaseUrl;

    /**
     * constructs a client with the default handle REST API base URL
     */
    public HandleRestApiResolver() {
        this(DEFAULT_HANDLE_API_URL);
    }

    /**
     *
     * @param handleApiBaseUrl base URL of the handle REST API (handle will be
     * directly appended to this)
     */
    public HandleRestApiResolver(String handleApiBaseUrl) {
        this.handleApiBaseUrl = handleApiBaseUrl;
    }

    @Override
    public URI resolve(URI uri) throws InvalidHandleException {
        final String url = getUrl(getHandle(uri.toString()));
        if (url == null) {
            return null;
        }
        try {
            return new URI(url);
        } catch (URISyntaxException ex) {
            throw new InvalidHandleException("Handle resolution error - invalid URI: " + url, ex);
        }
    }

    private String getHandle(String uri) throws InvalidHandleException {
        if (uri.startsWith(HANDLE_PREFIX)) {
            return uri.substring(HANDLE_PREFIX.length());
        } else if (uri.startsWith(HANDLE_PROXY)) {
            final String handle = uri.substring(HANDLE_PROXY.length());
            if (handle.startsWith(HANDLE_PREFIX)) {
                // legal case, e.g.: http://hdl.handle.net/hdl:1839/00-0000-0000-0000-0000-4
                return handle.substring(HANDLE_PREFIX.length());
            } else {
                return handle;
            }
        } else {
            throw new InvalidHandleException("Invalid handle: " + uri.toString());
        }
    }

    /**
     *
     * @param handle handle to resolve
     * @return the ULR provided by the handle server or null if it could not be
     * retrieved or is not available
     */
    public String getUrl(String handle) {
        final String requestUrl = handleApiBaseUrl + handle;
        logger.debug("Making request to {}", requestUrl);

        final Client client = Client.create();
        final WebResource resource = client.resource(requestUrl);

        try {
            final ClientResponse response = resource
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(ClientResponse.class);

            if (Response.Status.OK.getStatusCode() != response.getStatus()) {
                final Response.StatusType statusInfo = response.getStatusInfo();
                logger.error("Unexpected response status {} - {} for {}", statusInfo.getStatusCode(), statusInfo.getReasonPhrase(), requestUrl);
            } else {
                final String responseString = response.getEntity(String.class);
                return getUrlFromJson(responseString);
            }
        } catch (UniformInterfaceException ex) {
            logger.error("Could not communicate with Handle API", ex);
        } catch (ClientHandlerException ex) {
            logger.error("Could not communicate with Handle API", ex);
        } catch (JSONException ex) {
            logger.error("Could not parse Handle API response", ex);
        }
        return null;
    }

    public String getUrlFromJson(final String jsonString) throws JSONException {
        // The handle API returns a JSON structure with a number of handle
        // record fields. We are only interested in the value at
        // values[x].data.value where values[x].type == 'URL'

        final JSONObject jsonResponse = new JSONObject(jsonString);
        final JSONArray valuesArray = jsonResponse.getJSONArray("values");
        for (int i = 0; i < valuesArray.length(); i++) {
            final JSONObject object = valuesArray.getJSONObject(i);
            final String type = object.getString("type");
            if ("URL".equals(type) && object.has("data")) {
                final JSONObject data = object.getJSONObject("data");
                if (data.has("value")) {
                    // the field we were looking for
                    return data.getString("value");
                }
            }
        }
        // no URL field??
        logger.error("Handle API response did not incude a URL field");
        return null;
    }
}
