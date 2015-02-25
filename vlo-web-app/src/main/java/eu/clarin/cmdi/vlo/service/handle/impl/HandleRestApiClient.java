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
package eu.clarin.cmdi.vlo.service.handle.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import eu.clarin.cmdi.vlo.service.handle.HandleClient;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that connects to the handle.net REST API and retrieves the URL for a
 * given handle.
 *
 * Consider re-implementing using the handle API
 *
 * @author twagoo
 */
public class HandleRestApiClient implements HandleClient {

    private final static Logger logger = LoggerFactory.getLogger(HandleRestApiClient.class);

    private final String handleApiBaseUrl;

    /**
     * constructs a client with the default handle REST API base URL
     */
    public HandleRestApiClient() {
        //TODO: get from config
        this("http://hdl.handle.net/api/handles/");
    }

    /**
     *
     * @param handleApiBaseUrl base URL of the handle REST API (handle will be
     * directly appended to this)
     */
    public HandleRestApiClient(String handleApiBaseUrl) {
        this.handleApiBaseUrl = handleApiBaseUrl;
    }

    /**
     *
     * @param handle handle to resolve
     * @return the ULR provided by the handle server or null if it could not be
     * retrieved or is not available
     */
    @Override
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

    /**
     * {
     * "responseCode":1, "handle":"1839/00-0000-0000-0000-0000-4", "values": [{
     * "index":6, "type":"FILETIME", "data":"2014-03-25 08:35:11.0",
     * "ttl":86400, "timestamp":"1970-01-01T00:00:00Z" }, { "index":5,
     * "type":"CHECKSUM", "data":"696d818e19744f9f0290125e6385fa77",
     * "ttl":86400, "timestamp":"1970-01-01T00:00:00Z" }, { "index":4,
     * "type":"ONSITE", "data":"true", "ttl":86400,
     * "timestamp":"1970-01-01T00:00:00Z" }, { "index":3, "type":"FILESIZE",
     * "data":"6550", "ttl":86400, "timestamp":"1970-01-01T00:00:00Z"},
     * {"index":2,"type":"CRAWLTIME","data":"2014-03-25
     * 08:35:11.39","ttl":86400,"timestamp":"1970-01-01T00:00:00Z"},
     * {"index":1,"type":"URL","data":"http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi","ttl":86400,"timestamp":"1970-01-01T00:00:00Z"},
     * {"index":100,"type":"HS_ADMIN","data":{"format":"admin","value":{"handle":"0.NA/1839","index":200,"permissions":"010001110000"}},"ttl":86400,"timestamp":"1970-01-01T00:00:00Z"}
     * ] }
     */
}
