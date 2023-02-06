/*
 * Copyright (C) 2022 twagoo
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
package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.ximpleware.VTDGen;
import eu.clarin.cmdi.vlo.mapping.model.ContextImpl;
import java.net.URL;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author twagoo
 */
public class VTDValueContextTest {

    /**
     * Test of matchesXPath method, of class ValueContextImpl.
     */
    @Test
    public void testMatchesXPathTrue() {
        final String contextXPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date/text()";
        final VTDValueContext instance = createInstance("/records/p_1288172614026.cmdi", "clarin.eu:cr1:p_1288172614026", contextXPath);

        assertTrue(instance.matchesXPath(contextXPath), "canonical path");
        assertTrue(instance.matchesXPath("//cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date/text()"), "skip root");
        assertTrue(instance.matchesXPath("//cmdp:OLAC-DcmiTerms/cmdp:date/text()"), "skip root component");
        assertTrue(instance.matchesXPath("//cmdp:*/cmdp:date/text()"), "skip components in namespace, using wildcard");
        assertTrue(instance.matchesXPath("/cmd:CMD//cmdp:date/text()"), "skip intermediate");
        assertTrue(instance.matchesXPath("//cmdp:date/text()"), "skip all parents");
        assertTrue(instance.matchesXPath("//node()"), "//node()");
        assertTrue(instance.matchesXPath("//cmdp:date[@dcterms-type]/text()"), "select with attribute");
        assertTrue(instance.matchesXPath("//cmdp:date[@dcterms-type=\"W3CDTF\"]/text()"), "select with attribute value");
        assertTrue(instance.matchesXPath("//cmdp:date[@*]/text()"), "select with any attribute");
    }
    /**
     * Test of matchesXPath method, of class ValueContextImpl.
     */
    @Test
    public void testMatchesXPathTrueAttribute() {
        final String contextXPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:format/@dcterms-type";
        final VTDValueContext instance = createInstance("/records/p_1288172614026.cmdi", "clarin.eu:cr1:p_1288172614026", contextXPath);

        assertTrue(instance.matchesXPath(contextXPath), "canonical path");
        assertTrue(instance.matchesXPath("//cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:format/@dcterms-type"), "skip root");
        assertTrue(instance.matchesXPath("//cmdp:OLAC-DcmiTerms/cmdp:format/@dcterms-type"), "skip component root");
        assertTrue(instance.matchesXPath("//cmdp:*/cmdp:format/@dcterms-type"), "skip component root, using wildcard");
        assertTrue(instance.matchesXPath("/cmd:CMD//cmdp:format/@dcterms-type"), "skip intermediate");
        assertTrue(instance.matchesXPath("//cmdp:format/@dcterms-type"), "skip all element parents");
        assertTrue(instance.matchesXPath("//cmdp:*/@dcterms-type"), "element wilcard within namespace");
        assertTrue(instance.matchesXPath("//cmdp:*/@*"), "element wilcard within namespace + attribute wildcard");
        assertTrue(instance.matchesXPath("//@*"), "attribute wilcard");
        assertTrue(instance.matchesXPath("//node()/@*"), "//node() + attribute wildcard");
    }

    /**
     * Test of matchesXPath method, of class ValueContextImpl.
     */
    @Test
    public void testMatchesXPathFalse() {
        
        final String contextXPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date/text()";

        final VTDValueContext instance = createInstance("/records/p_1288172614026.cmdi", "clarin.eu:cr1:p_1288172614026", contextXPath);
        {
            final String mismatchPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:language/text()";
            assertFalse(instance.matchesXPath(mismatchPath), "sibling node with different path");
        }
        {
            final String mismatchPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date";
            assertFalse(instance.matchesXPath(mismatchPath), "parent node");
        }
        {
            final String mismatchPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:foo/text()";
            assertFalse(instance.matchesXPath(mismatchPath), "node does not exist");
        }
        {
            final String mismatchPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date/@dcterms-type";
            assertFalse(instance.matchesXPath(mismatchPath), "child attribute");
        }
        {
            final String mismatchPath = "/cmd:CMD/cmd:Components/cmdp:OLAC-DcmiTerms/cmdp:date[";
            assertFalse(instance.matchesXPath(mismatchPath), "xpath syntax error");
        }
    }

    private VTDValueContext createInstance(final String resource, String profileId, final String contextXPath) {
        final URL resourceUri = getClass().getResource(resource);
        final VTDGen vg = new VTDGen();
        vg.parseFile(resourceUri.getFile(), true);
        final ContextImpl context = new ContextImpl(contextXPath, null, null);
        final VTDValueContext instance = new VTDValueContext(context, null, profileId, vg.getNav());
        return instance;
    }

}