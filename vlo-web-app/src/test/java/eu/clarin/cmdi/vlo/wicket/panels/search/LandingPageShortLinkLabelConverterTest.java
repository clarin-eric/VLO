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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class LandingPageShortLinkLabelConverterTest {

    private LandingPageShortLinkLabelConverter instance;

    @Before
    public void setUp() {
        instance = new LandingPageShortLinkLabelConverter();
    }

    /**
     * Test of convertToString method, of class
     * LandingPageShortLinkLabelConverter.
     */
    @Test
    public void testConvertHandle() {
        assertEquals("1234/5678", instance.convertToString("hdl:1234/5678", null));
        assertEquals("1234/5678", instance.convertToString("http://hdl.handle.net/1234/5678", null));
        assertEquals("1234/5678", instance.convertToString("https://hdl.handle.net/1234/5678", null));
    }

    @Test
    public void testHostname() {
        assertEquals("www.clarin.eu", instance.convertToString("http://www.clarin.eu", null));
        assertEquals("www.clarin.eu", instance.convertToString("https://www.clarin.eu", null));
        assertEquals("www.clarin.eu", instance.convertToString("http://www.clarin.eu/", null));
        assertEquals("www.clarin.eu", instance.convertToString("https://www.clarin.eu/", null));
        assertEquals("www.clarin.eu", instance.convertToString("http://www.clarin.eu/my/resource", null));
        assertEquals("www.clarin.eu", instance.convertToString("https://www.clarin.eu/my/resource", null));
    }

    @Test
    public void testOther() {
        assertEquals("ftp://ftp.clarin.eu/uncommon/case", instance.convertToString("ftp://ftp.clarin.eu/uncommon/case", null));
    }
}
