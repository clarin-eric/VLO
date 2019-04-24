/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ResourceInfoTest {

    private final static String JSON = "{"
            + "\"url\": \"urlValue\","
            + "\"type\": \"typeValue\","
            + "\"status\":\"statusValue\","
            + "\"lastChecked\":1580558400000"
            + "}";

    private final static long LAST_CHECKED_TIME = 1580558400000L;
    private final static ResourceInfo RESOURCE_INFO = new ResourceInfo("urlValue", "typeValue", "statusValue", LAST_CHECKED_TIME);

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testToJson() {
        final String result = RESOURCE_INFO.toJson(objectMapper);
        assertNotNull(result);
        assertTrue(result.contains("\"url\":"));
        assertTrue(result.contains("urlValue"));
        assertTrue(result.contains("\"type\":"));
        assertTrue(result.contains("typeValue"));
        assertTrue(result.contains("\"status\":"));
        assertTrue(result.contains("statusValue"));
        assertTrue(result.contains("\"lastChecked\""));
        assertTrue(result.contains(Long.toString(LAST_CHECKED_TIME)));
    }

    @Test
    public void testFromJson() {
        final ResourceInfo result = ResourceInfo.fromJson(objectMapper, JSON);
        assertEquals(RESOURCE_INFO.getUrl(), result.getUrl());
        assertEquals(RESOURCE_INFO.getType(), result.getType());
        assertEquals(RESOURCE_INFO.getStatus(), result.getStatus());
        assertEquals(RESOURCE_INFO.getLastChecked(), result.getLastChecked());
    }

}
