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
import com.google.common.collect.Iterables;
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

    private final static List<String> VALID_DOIS
            = ImmutableList.of(
                    "http://doi.org/123/456",
                    "https://doi.org/123/456",
                    "https://DOI.ORG/123/456",
                    "doi:123/456",
                    "DOI:123/456",
                    "http://dx.doi.org/123/456",
                    "https://dx.doi.org/123/456",
                    "https://DX.doi.ORG/123/456");

    private final static List<String> VALID_URN_NBNS
            = ImmutableList.of(
                    "urn:nbn:de:bvb:19-146642",
                    "urn:nbn:fi:lb-2015120101",
                    "http://urn.fi/urn:nbn:fi:lb-2017021504",
                    "http://nbn-resolving.de/urn:nbn:de:kobv:b4-200905195090");

    private final static List<String> NOT_PIDS
            = ImmutableList.of(
                    "",
                    "http://www.google.com",
                    "abcd");

    private final static List<String> NOT_HANDLES
            = ImmutableList.of(
                    "",
                    "doi:10.1038/nphys1170",
                    "http://dx.doi.org/10.1038/nphys1170",
                    "urn:nbn:de:bvb:19-146642");

    private final static List<String> NOT_DOIS
            = ImmutableList.of(
                    "hdl:123/456",
                    "http://hdl.handle.net/123/456",
                    "https://hdl.handle.net/123/456",
                    "urn:nbn:de:bvb:19-146642");

    private final static List<String> NOT_URN_NBNS
            = ImmutableList.of(
                    "doi:10.1038/nphys1170",
                    "http://dx.doi.org/10.1038/nphys1170",
                    "hdl:123/456",
                    "http://hdl.handle.net/123/456",
                    "https://hdl.handle.net/123/456");

    private final static List<String> ACTIONABLE_PIDS
            = ImmutableList.of(
                    "http://hdl.handle.net/123/456",
                    "https://hdl.handle.net/123/456",
                    "HTTP://hdl.HANDLE.net/123/456",
                    "https://HDL.handle.NET/123/456",
                    "http://doi.org/123/456",
                    "https://doi.org/123/456",
                    "https://DOI.ORG/123/456",
                    "http://dx.doi.org/123/456",
                    "https://dx.doi.org/123/456",
                    "https://DX.doi.ORG/123/456",
                    "http://urn.fi/urn:nbn:fi:lb-2017021504",
                    "http://nbn-resolving.de/urn:nbn:de:kobv:b4-200905195090");

    private final static List<String> NON_ACTIONABLE_PIDS
            = ImmutableList.of(
                    "hdl:123/456",
                    "HDL:123/456",
                    "doi:10.1038/nphys1170",
                    "urn:nbn:de:bvb:19-146642");

    private final static Iterable<String> ACTIONABLE
            = Iterables.concat(ACTIONABLE_PIDS, ImmutableList.of(
                    "https://wwww.clarin.eu",
                    "http://wwww.clarin.eu"));

    private final static List<String> INVALID // generally non-actionable or resolvable
            = ImmutableList.of(
                    "",
                    "abcd",
                    "http:",
                    "//");

    /**
     * Test of isPid method, of class PIDUtils.
     */
    @Test
    public void testIsPid() {
        VALID_HANDLES.forEach(h -> assertTrue(h, PIDUtils.isPid(h)));
        VALID_DOIS.forEach(h -> assertTrue(h, PIDUtils.isPid(h)));
        VALID_URN_NBNS.forEach(h -> assertTrue(h, PIDUtils.isPid(h)));

        //negatives
        NOT_PIDS.forEach(h -> assertFalse(h, PIDUtils.isPid(h)));
        assertFalse("null", PIDUtils.isPid(null));
    }

    /**
     * Test of isHandle method, of class PIDUtils.
     */
    @Test
    public void testIsHandle() {
        VALID_HANDLES.forEach(h -> assertTrue(h, PIDUtils.isHandle(h)));

        //negatives
        NOT_HANDLES.forEach(h -> assertFalse(h, PIDUtils.isHandle(h)));
        NOT_PIDS.forEach(h -> assertFalse(h, PIDUtils.isHandle(h)));
        assertFalse("null", PIDUtils.isHandle(null));
    }

    /**
     * Test of isHandle method, of class PIDUtils.
     */
    @Test
    public void testIsDoi() {
        VALID_DOIS.forEach(h -> assertTrue(h, PIDUtils.isDoi(h)));

        //negatives
        NOT_DOIS.forEach(h -> assertFalse(h, PIDUtils.isDoi(h)));
        NOT_PIDS.forEach(h -> assertFalse(h, PIDUtils.isDoi(h)));
        assertFalse("null", PIDUtils.isDoi(null));
    }

    /**
     * Test of isHandle method, of class PIDUtils.
     */
    @Test
    public void testIsUrnNbn() {
        VALID_URN_NBNS.forEach(h -> assertTrue(h, PIDUtils.isUrnNbn(h)));

        //negatives
        NOT_URN_NBNS.forEach(h -> assertFalse(h, PIDUtils.isUrnNbn(h)));
        NOT_PIDS.forEach(h -> assertFalse(h, PIDUtils.isUrnNbn(h)));
        assertFalse("null", PIDUtils.isUrnNbn(null));
    }

    @Test
    public void testGetSchemeSpecificId() {
        VALID_HANDLES.forEach(h -> assertNotNull(PIDUtils.getSchemeSpecificId(h)));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("hdl:1234/5678"));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("http://HDL.handle.NET/1234/5678"));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("HTTPS://hdl.HANDLE.net/1234/5678"));

        VALID_DOIS.forEach(h -> assertNotNull(PIDUtils.getSchemeSpecificId(h)));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("doi:1234/5678"));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("http://DOI.org/1234/5678"));
        assertEquals("1234/5678", PIDUtils.getSchemeSpecificId("HTTPS://dx.doi.org/1234/5678"));

        VALID_URN_NBNS.forEach(h -> assertNotNull(PIDUtils.getSchemeSpecificId(h)));
        assertEquals("fi:1234/5678", PIDUtils.getSchemeSpecificId("urn:nbn:fi:1234/5678"));
        assertEquals("fi:1234/5678", PIDUtils.getSchemeSpecificId("http://urn.fi/urn:nbn:fi:1234/5678"));
        assertEquals("de:123:456", PIDUtils.getSchemeSpecificId("http://nbn-resolving.de/urn:nbn:de:123:456"));

        //negatives
        NOT_PIDS.forEach(h -> assertNull(PIDUtils.getSchemeSpecificId(h)));
        assertNull(PIDUtils.getSchemeSpecificId(null));
    }

    @Test
    public void testIsActionable() {
        ACTIONABLE.forEach(h -> assertTrue(PIDUtils.isActionableLink(h)));
        NON_ACTIONABLE_PIDS.forEach(h -> assertFalse(PIDUtils.isActionableLink(h)));
        INVALID.forEach(h -> assertFalse(PIDUtils.isActionableLink(h)));
        ACTIONABLE.forEach(h -> assertFalse("input should be trimmed", PIDUtils.isActionableLink(" " + h + " ")));
    }

    @Test
    public void testGetActionableLinkForPid() {
        ACTIONABLE_PIDS.forEach(h -> assertEquals("actionable pids should not be changed", h, PIDUtils.getActionableLinkForPid(h)));
        ACTIONABLE.forEach(h -> assertEquals("actionable links should not be changed", h, PIDUtils.getActionableLinkForPid(h)));
        NON_ACTIONABLE_PIDS.forEach(h -> assertNotEquals("non-actionable PIDs should be changed into links", h, PIDUtils.getActionableLinkForPid(h)));
        INVALID.forEach(h -> assertEquals("invalid links should not be changed", h, PIDUtils.getActionableLinkForPid(h)));
    }

}
