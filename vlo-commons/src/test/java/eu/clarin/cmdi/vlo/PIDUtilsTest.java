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
package eu.clarin.cmdi.vlo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDUtilsTest {

    private final static List<String> VALID_HANDLES
            = ImmutableList.of(
                    "hdl:123/456",
                    "http://hdl.handle.net/123/456",
                    "https://hdl.handle.net/123/456",
                    "HDL:123/456",
                    "HTTP://hdl.HANDLE.net/123/456",
                    "https://HDL.handle.NET/123/456");

    private final static List<String> NOT_HANDLES
            = ImmutableList.of(
                    "",
                    "http://www.google.com",
                    "abcd",
                    "doi:10.1038/nphys1170",
                    "http://dx.doi.org/10.1038/nphys1170");

    private final static List<String> NOT_PIDS
            = ImmutableList.of(
                    "",
                    "http://www.google.com",
                    "abcd");

    /**
     * Test of isPid method, of class PIDUtils.
     */
    @Test
    public void testIsPid() {
        VALID_HANDLES.forEach(h -> assertTrue(PIDUtils.isPid(h)));

        //negatives
        NOT_PIDS.forEach(h -> assertFalse(PIDUtils.isPid(h)));
        assertFalse(PIDUtils.isPid(null));
    }

    /**
     * Test of isHandle method, of class PIDUtils.
     */
    @Test
    public void testIsHandle() {
        VALID_HANDLES.forEach(h -> assertTrue(PIDUtils.isHandle(h)));

        //negatives
        NOT_HANDLES.forEach(h -> assertFalse(PIDUtils.isHandle(h)));
        NOT_PIDS.forEach(h -> assertFalse(PIDUtils.isHandle(h)));
        assertFalse(PIDUtils.isHandle(null));
    }

    @Test
    public void testGetSchemeSpecificId() {
        VALID_HANDLES.forEach(h -> assertNotNull(PIDUtils.getSchemeSpecificId(h)));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("hdl:1234/5678"));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("http://HDL.handle.NET/1234/5678"));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("HTTPS://hdl.HANDLE.net/1234/5678"));

        //negatives
        NOT_PIDS.forEach(h -> assertNull(PIDUtils.getSchemeSpecificId(h)));
        assertNull(PIDUtils.getSchemeSpecificId(null));
    }

}
