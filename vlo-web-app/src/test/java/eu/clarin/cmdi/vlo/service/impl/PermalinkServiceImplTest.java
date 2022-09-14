/*
 * Copyright (C) 2022 CLARIN
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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class PermalinkServiceImplTest {

    public PermalinkServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getUrlString method, of class PermalinkServiceImpl.
     */
    @Test
    public void testCleanup() {
        {
            String input = "https://vlo.clarin.eu/record/http_58__47__47_hdl.handle.net_47_11022_47_0000-0000-5050-B";
            String target = input;
            String result = PermalinkServiceImpl.UrlCleaner.cleanUp(input);
            assertEquals(target, result);
        }
        {
            String input = "https://vlo.clarin.eu/record/http_58__47__47_hdl.handle.net_47_11022_47_0000-0000-5050-B?17&index=4&count=26&tab=resources";
            String target = input;
            String result = PermalinkServiceImpl.UrlCleaner.cleanUp(input);
            assertEquals(target, result);
        }
        {
            String input = "https://vlo.clarin.eu/record/http_58__47__47_hdl.handle.net_47_11022_47_0000-0000-5050-B;jsessionid=F0F0E9EC1933EBB0287E76DAEAFD9ABF";
            String target = "https://vlo.clarin.eu/record/http_58__47__47_hdl.handle.net_47_11022_47_0000-0000-5050-B";
            String result = PermalinkServiceImpl.UrlCleaner.cleanUp(input);
            assertEquals(target, result);
        }
        {
            String input = "https://vlo.clarin.eu/record/http_58__47__47_hdl.handle.net_47_11022_47_0000-0000-5050-B;jsessionid=F0F0E9EC1933EBB0287E76DAEAFD9ABF?17&index=4&count=26&tab=resources";
            String target = "https://vlo.clarin.eu/record/http_58__47__47_hdl.handle.net_47_11022_47_0000-0000-5050-B?17&index=4&count=26&tab=resources";
            String result = PermalinkServiceImpl.UrlCleaner.cleanUp(input);
            assertEquals(target, result);
        }
        {
            String input = "http://localhost:8080/vlo-web-app/search;jsessionid=000E370FDB4B5158107530C1E04CA876?fqType=languageCode:or&fq=languageCode:code:nio";
            String target = "http://localhost:8080/vlo-web-app/search?fqType=languageCode:or&fq=languageCode:code:nio";
            String result = PermalinkServiceImpl.UrlCleaner.cleanUp(input);
            assertEquals(target, result);
        }
    }

}
