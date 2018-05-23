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
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.clarin.cmdi.vlo.VloWebAppException;
import eu.clarin.cmdi.vlo.service.RatingStore;
import eu.clarin.cmdi.vlo.wicket.model.RatingLevel;
import java.util.UUID;
import javax.ws.rs.core.Response.Status;
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

    public CouchDbRatingStore(String ratingsBaseUri, String userName, String password) {
        this.ratingsBaseUri = ratingsBaseUri;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public void storeRating(RatingLevel rating, String comment) throws VloWebAppException {
        if (Strings.isNullOrEmpty(ratingsBaseUri)) {
            throw new VloWebAppException("CouchDB rating store not configured (base URL not set)");
        } else {
            final long now = System.currentTimeMillis();
            logger.debug("Storing rating and comment: '{}', '{}', '{}'", now, rating.getDescription(), comment);
            store(now, rating, comment);
        }
    }

    private void store(long now, RatingLevel rating, String comment) throws VloWebAppException {
        final Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(userName, password));
        try {
            //generate a unique ID for the rating
            final String id = UUID.randomUUID().toString();

            //create the rating object as JSON
            final JSONObject ratingObject = new JSONObject()
                    .put("timestamp", System.currentTimeMillis())
                    .put("rating_value", rating.getValue())
                    .put("rating_description", rating.getDescription())
                    .put("comment", comment);

            logger.debug("Sending object to server: {} with id {}", ratingObject, id);

            //send off to store
            final ClientResponse response
                    = client.resource(ratingsBaseUri)
                            .path(id)
                            .put(ClientResponse.class, ratingObject.toString());

            //verify response (should be 201 CREATED)
            if (response.getStatus() != Status.CREATED.getStatusCode()) {
                throw new VloWebAppException("Unexpected response to rating PUT: " + response.getStatus() + " - " + response.getEntity(String.class));
            } else {
                logger.debug("Server response: {}", response.getEntity(String.class));
            }
        } catch (ClientHandlerException ex) {
            throw new VloWebAppException("Exception while trying to push to CouchDB server", ex);
        } finally {
            client.destroy();
        }
    }

}
