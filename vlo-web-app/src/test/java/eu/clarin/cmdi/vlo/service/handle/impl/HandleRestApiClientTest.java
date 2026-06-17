/*
 * Copyright (C) 2026 CLARIN
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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author twagoo
 */
public class HandleRestApiClientTest {

    /**
     * Test of getUrl method, of class HandleRestApiClient.
     */
    @Test
    public void testGetUrl() {
        System.out.println("getUrl");
        String handle = "11022/0000-0007-C9C6-7";
        HandleRestApiClient instance = new HandleRestApiClient();
        String result = instance.getUrl(handle);
        assertNotNull(result);
//        assertEquals(expResult, result);
    }

    /**
     * Test of getUrlFromJson method, of class HandleRestApiClient.
     */
    @Test
    public void testGetUrlFromJson() {
        System.out.println("getUrlFromJson");
        String jsonString = """
                            {"responseCode":1,"handle":"11022/0000-0007-C9C6-7","values":[{"index":1,"type":"URL","data":{"format":"string","value":"https://doi.org/10.25592/uhhfdm.11120"},"ttl":86400,"timestamp":"2023-09-15T11:06:47Z"},{"index":2,"type":"INST","data":{"format":"string","value":"1008"},"ttl":86400,"timestamp":"2018-06-18T12:39:13Z"},{"index":100,"type":"HS_ADMIN","data":{"format":"admin","value":{"handle":"0.NA/11022","index":1008,"permissions":"011111110011"}},"ttl":86400,"timestamp":"2018-06-18T12:39:13Z"}]}
                            """;

        String expResult = "https://doi.org/10.25592/uhhfdm.11120";
        String result = HandleRestApiClient.getUrlFromJson(jsonString);
        assertEquals(expResult, result);
    }

}
