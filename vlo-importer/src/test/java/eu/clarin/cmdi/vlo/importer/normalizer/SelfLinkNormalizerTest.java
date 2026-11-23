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
package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.importer.processor.FacetValuesMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author twagoo
 */
public class SelfLinkNormalizerTest {

    private SelfLinkNormalizer instance;

    @BeforeEach
    public void setUp() {
        instance = new SelfLinkNormalizer();
    }

    /**
     * Test of process method, of class SelfLinkNormalizer.
     */
    @Test
    public void testProcessNull() {
        final String result = process(null);
        assertNull(result);
    }

    @Test
    public void testProcessEmpty() {
        final String result = process("");
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void testProcessHttp() {
        final String result = process("http://www.clarin.eu");
        assertNotNull(result);
        assertEquals("http://www.clarin.eu", result, "http should remain unchanged as http");
    }

    @Test
    public void testProcessHTTP() {
        final String result = process("HTTP://www.clarin.eu");
        assertNotNull(result);
        assertEquals("http://www.clarin.eu", result, "HTTP upper case should become lower case http");
    }

    @Test
    public void testProcessHttps() {
        final String result = process("https://www.clarin.eu");
        assertNotNull(result);
        assertEquals("http://www.clarin.eu", result, "https: should get normalised to http:");
    }

    @Test
    public void testProcessHTTPS() {
        final String result = process("https://www.clarin.eu");
        assertNotNull(result);
        assertEquals("http://www.clarin.eu", result, "HTTPS: should get normalised to http:");
    }

    @Test
    public void testProcessHandleHdl() {
        final String result = process("hdl:1234/5678");
        assertNotNull(result);
        assertEquals("hdl:1234/5678", result, "hdl: should remain unchanged as hdl:");
    }

    @Test
    public void testProcessHandleHDL() {
        final String result = process("HDL:1234/5678");
        assertNotNull(result);
        assertEquals("hdl:1234/5678", result, "HDL: should be normalized to hdl:");
    }

    @Test
    public void testProcessHandleHdlHttp() {
        final String result = process("http://hdl.handle.net/1234/5678");
        assertNotNull(result);
        assertEquals("hdl:1234/5678", result, "hdl.handle.net URL should get normalised to hdl:");
    }

    @Test
    public void testProcessHandleHdlHttps() {
        final String result = process("https://hdl.handle.net/1234/5678");
        assertNotNull(result);
        assertEquals("hdl:1234/5678", result, "hdl.handle.net URL should get normalised to hdl:");
    }

    @Test
    public void testProcessHandleDoi() {
        final String result = process("doi:10.0001/1234");
        assertNotNull(result);
        assertEquals("doi:10.0001/1234", result, "doi: should remain unchanged as doi:");
    }

    @Test
    public void testProcessHandleDOI() {
        final String result = process("DOI:10.0001/1234");
        assertNotNull(result);
        assertEquals("doi:10.0001/1234", result, "doi: should remain unchanged as doi:");
    }

    @Test
    public void testProcessHandleDoiHttp() {
        final String result = process("http://doi.org/10.0001/1234");
        assertNotNull(result);
        assertEquals("doi:10.0001/1234", result, "DOI URL should get normalised to doi:");
    }

    @Test
    public void testProcessHandleDoiHttps() {
        final String result = process("https://doi.org/10.0001/1234");
        assertNotNull(result);
        assertEquals("doi:10.0001/1234", result, "DOI URL should get normalised to doi:");
    }

    @Test
    public void testProcessHandleDoiHTTPS() {
        final String result = process("HTTPS://DOI.ORG/10.0001/1234");
        assertNotNull(result);
        assertEquals("doi:10.0001/1234", result, "DOI URL should get normalised to doi:");
    }

    @Test
    public void testProcessHandleDoiDx() {
        final String result = process("http://dx.doi.org/10.0001/1234");
        assertNotNull(result);
        assertEquals("doi:10.0001/1234", result, "DOI URL should get normalised to doi:");
        final String result2 = process("https://dx.doi.org/10.0001/1234");
        assertNotNull(result2);
        assertEquals("doi:10.0001/1234", result2, "DOI URL should get normalised to doi:");
    }

    private String process(String value) {
        final List<String> result = instance.process(value, new FacetValuesMap());
        assertNotNull(result);
        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }

    }

}
