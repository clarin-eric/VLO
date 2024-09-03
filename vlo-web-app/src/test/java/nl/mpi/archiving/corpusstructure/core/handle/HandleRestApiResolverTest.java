/*
 * Copyright (C) 2024 CLARIN
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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class HandleRestApiResolverTest {

    /**
     * Test of getUrlFromJson method, of class HandleRestApiResolver.
     */
    @Test
    public void testGetUrlFromJson() throws Exception {
        String handle = "11022/1009-0000-0007-F9B1-8";
        String jsonString = "{\"responseCode\":1,\"handle\":\"11022/1009-0000-0007-F9B1-8\",\"values\":[{\"index\":1,\"type\":\"URL\",\"data\":{\"format\":\"string\",\"value\":\"https://clarin.phonetik.uni-muenchen.de/BASRepository/Public/Corpora/SKAUDIO/SKAUDIO.1.php\"},\"ttl\":86400,\"timestamp\":\"2022-12-23T14:06:57Z\"},{\"index\":2,\"type\":\"INST\",\"data\":{\"format\":\"string\",\"value\":\"1009\"},\"ttl\":86400,\"timestamp\":\"2022-12-23T14:06:57Z\"},{\"index\":100,\"type\":\"HS_ADMIN\",\"data\":{\"format\":\"admin\",\"value\":{\"handle\":\"0.NA/11022\",\"index\":1009,\"permissions\":\"011111110011\"}},\"ttl\":86400,\"timestamp\":\"2022-12-23T14:06:57Z\"}]}";

        HandleRestApiResolver instance = new HandleRestApiResolver();
        String expResult = "https://clarin.phonetik.uni-muenchen.de/BASRepository/Public/Corpora/SKAUDIO/SKAUDIO.1.php";
        String result = instance.getUrlFromJson(handle, jsonString);
        assertNotNull(result);
        assertEquals(expResult, result);
    }

    private boolean aliasRequestMade;

    /**
     * Test of getUrlFromJson method, of class HandleRestApiResolver.
     */
    @Test
    public void testGetUrlFromJsonWithAlias() throws Exception {
        final String handle = "11858/00-1734-0000-0002-16D6-5";
        final String ALIAS_HANDLE = "21.11113/00-1734-0000-0002-16D6-5";
        final String TARGET_URL = "http://textgridrep.org/textgrid:k98p.0";
        final String jsonString = "{\"responseCode\":1,\"handle\":\"11858/00-1734-0000-0002-16D6-5\",\"values\":[{\"index\":1,\"type\":\"HS_ALIAS\",\"data\":{\"format\":\"string\",\"value\":\"" + ALIAS_HANDLE + "\"},\"ttl\":86400,\"timestamp\":\"2017-10-05T13:58:46Z\"},{\"index\":8,\"type\":\"DISABLED_CREATOR\",\"data\":{\"format\":\"string\",\"value\":\"1734\"},\"ttl\":86400,\"timestamp\":\"2017-10-05T13:58:46Z\"},{\"index\":100,\"type\":\"HS_ADMIN\",\"data\":{\"format\":\"admin\",\"value\":{\"handle\":\"0.NA/11858\",\"index\":200,\"permissions\":\"010001110000\",\"legacyByteLength\":true}},\"ttl\":86400}]}";

        aliasRequestMade = false;

        final HandleRestApiResolver instance = new HandleRestApiResolver() {
            @Override
            public String getUrl(String handle) {
                // mock this response to retrieve the alias which should be called by getUrlFromJson
                if (handle.equals(ALIAS_HANDLE)) {
                    aliasRequestMade = true;
                    return TARGET_URL;
                } else {
                    return super.getUrl(handle);
                }
            }

        };
        String expResult = TARGET_URL;
        String result = instance.getUrlFromJson(handle, jsonString);
        assertNotNull(result);
        assertEquals(expResult, result);

        // follow-up request should have been for the alias!
        assertTrue(aliasRequestMade);
    }

}
