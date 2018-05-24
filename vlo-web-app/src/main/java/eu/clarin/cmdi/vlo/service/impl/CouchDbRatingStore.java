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

import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.clarin.cmdi.vlo.VloWebAppException;
import eu.clarin.cmdi.vlo.service.RatingStore;
import eu.clarin.cmdi.vlo.wicket.model.RatingLevel;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response.Status;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class CouchDbRatingStore implements RatingStore {
    
    public static final Logger logger = LoggerFactory.getLogger(CouchDbRatingStore.class);
    
    private final String ratingsBaseUri;
    private final String userName;
    private final String password;
    private final String serviceName;
    
    public CouchDbRatingStore(String ratingsBaseUri, String userName, String password, String serviceName) {
        this.ratingsBaseUri = ratingsBaseUri;
        this.userName = userName;
        this.password = password;
        this.serviceName = serviceName;
    }
    
    @PostConstruct
    public void initResource() {
        if (Strings.isNullOrEmpty(ratingsBaseUri)) {
            logger.info("CouchDB rating store not configured (base URL not set). Will not try to initialise.");
        } else {
            //check if resource exists at URI; if  not, try to create it
            final Client client = newClient();
            try {
                final WebResource resource = client.resource(ratingsBaseUri);
                final ClientResponse getResponse = resource.get(ClientResponse.class);
                if (getResponse.getStatus() == Status.NOT_FOUND.getStatusCode()) {
                    //resource does NOT exist; try to create it
                    if (!instantiateResource(resource)) {
                        throw new RuntimeException("Failed to create resource " + ratingsBaseUri + " for " + getClass().getSimpleName());
                    }
                } else {
                    logger.debug("Resource found at {}", ratingsBaseUri);
                }
            } finally {
                client.destroy();
            }
        }
    }
    
    @Override
    public void storeRating(RatingLevel rating, String comment, String host) throws VloWebAppException {
        if (Strings.isNullOrEmpty(ratingsBaseUri)) {
            throw new VloWebAppException("CouchDB rating store not configured (base URL not set)");
        } else {
            final long now = System.currentTimeMillis();
            logger.debug("Storing rating and comment: '{}', '{}', '{}'", now, rating.getDescription(), comment);
            store(now, rating, comment, host);
        }
    }
    
    private void store(long timestamp, RatingLevel rating, String comment, String host) throws VloWebAppException {
        final Client client = newClient();
        try {
            //generate a unique ID for the rating
            final String id = UUID.randomUUID().toString();

            //create object to put
            final JSONObject ratingObject = createJsonForRating(timestamp, rating, comment, host);

            //send off to store
            logger.debug("Sending object to server: {} with id {}", ratingObject, id);
            putRating(client, id, ratingObject, true);
        } catch (ClientHandlerException | UniformInterfaceException ex) {
            throw new VloWebAppException("Exception while trying to push to CouchDB server", ex);
        } finally {
            client.destroy();
        }
    }
    
    private JSONObject createJsonForRating(long timestamp, RatingLevel rating, String comment, String host) throws JSONException {
        //create the rating object as JSON
        return new JSONObject()
                .put("timestamp", timestamp)
                .put("service", serviceName)
                .put("host", host)
                .put("rating_value", rating.getValue())
                .put("rating_description", rating.getDescription())
                .put("comment", comment);
    }
    
    private void putRating(final Client client, final String id, final JSONObject ratingObject, boolean createIfNotExists) throws VloWebAppException, ClientHandlerException, UniformInterfaceException {
        final ClientResponse response
                = client.resource(ratingsBaseUri)
                        .path(id)
                        .put(ClientResponse.class, ratingObject.toString());

        //verify response (should be 201 CREATED)
        if (createIfNotExists && response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
            logger.warn("Resource {} was not found while trying to PUT a rating", ratingsBaseUri);
            if (instantiateResource(client.resource(ratingsBaseUri))) {
                //try again (without trying to create again even though we should not get into a loop you never know)
                putRating(client, id, ratingObject, false);
            } else {
                throw new VloWebAppException("Could not PUT rating as the resource was not found and could not be created!");
            }
        } else if (response.getStatus() != Status.CREATED.getStatusCode()) {
            throw new VloWebAppException("Unexpected response to rating PUT: " + response.getStatus() + " - " + response.getEntity(String.class));
        } else {
            logger.debug("Server response: {}", response.getEntity(String.class));
        }
    }
    
    private Client newClient() {
        final Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(userName, password));
        return client;
    }

    /**
     * Create the specified resource via a plain PUT
     *
     * @param resource
     * @throws RuntimeException
     * @throws UniformInterfaceException
     * @throws ClientHandlerException
     */
    private boolean instantiateResource(final WebResource resource) throws RuntimeException, UniformInterfaceException, ClientHandlerException {
        logger.info("Resource {} not found. Will try to create it via PUT.", ratingsBaseUri);
        final ClientResponse putResponse = resource.put(ClientResponse.class);
        if (putResponse.getStatus() == Status.CREATED.getStatusCode()) {
            logger.info("Successfully created resource at {}", ratingsBaseUri);
            return true;
        } else {
            logger.error("Could not create resource {} via PUT", ratingsBaseUri);
            return false;
        }
    }
    
}
