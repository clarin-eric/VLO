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

import com.google.common.collect.Iterators;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.rethinkdb.RethinkDB.r;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import eu.clarin.cmdi.vlo.wicket.model.RatingLevel;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Ignore("Can only be executed with a Rethink engine running")
public class RethinkRatingStoreTest {

    private static final int PORT = 32772;
    private static final String HOST = "localhost";
    private static final String DB = "rating_vlo";
    private static final String TABLE = "rating";

    private RethinkRatingStore instance;
    private Connection conn;

    @Before
    public void setUp() {
        instance = new RethinkRatingStore(HOST, PORT, DB);
        conn = r.connection().hostname(HOST).port(PORT).db(DB).connect();
        //remove all items
        r.table(TABLE).delete().run(conn);
    }

    @After
    public void tearDown() {
        conn.close();
        conn = null;
    }

    /**
     * Test of storeRating method, of class RethinkRatingStore.
     */
    @Test
    public void testStoreRating() {
        try (Cursor cursor = r.table(TABLE).run(conn)) {
            assertEquals("Empty table", 0, Iterators.size(cursor));
        }
        instance.storeRating(RatingLevel.VERY_SATISFIED, "my comment");
        try (Cursor cursor = r.table(TABLE).run(conn)) {
            assertEquals("Inserted item", 1, Iterators.size(cursor));
        }
        try (Cursor<Map<String, Object>> cursor = r.table(TABLE).run(conn)) {
            final Map<String, Object> item = cursor.next();
            assertNotNull("Item should be a map", item);

            assertEquals(RatingLevel.VERY_SATISFIED.getValue(), item.get("rating_value"));
            assertEquals(RatingLevel.VERY_SATISFIED.getDescription(), item.get("rating_description"));
            assertEquals("my comment", item.get("comment"));
        }
    }

}
