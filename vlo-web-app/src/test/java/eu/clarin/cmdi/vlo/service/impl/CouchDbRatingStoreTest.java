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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import eu.clarin.cmdi.vlo.wicket.model.RatingLevel;
import java.io.IOException;
import java.io.Reader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Ignore("This will only work with an instance of CouchDB available")
public class CouchDbRatingStoreTest {

    private final String ratingsBaseUri = "http://localhost:5984/ratings";
    private final String userName = "vlo";
    private final String password = "olv";

    private CouchDbRatingStore instance;
    private Client client;

    @Before
    public void setUp() {
        instance = new CouchDbRatingStore(ratingsBaseUri, userName, password);
        client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(userName, password));
    }

    @After
    public void tearDown() {
        client.destroy();
        client = null;
    }

    /**
     * Test of storeRating method, of class CouchDbRatingStore.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testStoreRating() throws Exception {
        final long beforeCount = getDocCount();
        instance.storeRating(RatingLevel.VERY_SATISFIED, "my comment");
        assertEquals(beforeCount + 1, getDocCount());
    }

    private long getDocCount() throws ClientHandlerException, IOException, UniformInterfaceException, ParseException {
        final JSONObject jsonObject = (JSONObject) new JSONParser().parse(client.resource(ratingsBaseUri).get(Reader.class));
        final Number count = (Number) jsonObject.get("doc_count");
        return count.longValue();
    }

}
