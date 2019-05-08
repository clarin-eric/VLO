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
package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FormatPostNormalizerTest {

    private FormatPostNormalizer instance;

    @Before
    public void setUp() {
        instance = new FormatPostNormalizer();
    }

    /**
     * Test of process method, of class FormatPostNormalizer.
     */
    @Test
    public void testSimpleMime() {
        assertResult("text/plain", "text/plain");
        assertResult("audio/bla", "audio/bla");
    }

    /**
     * Test of process method, of class FormatPostNormalizer.
     */
    @Test
    public void testParamsMime() {
        assertResult("text/plain", "text/plain; charset=UTF-8");
        assertResult("text/plain", "text/plain; charset=\"us-ascii\"; format=flowed");
    }

    @Test
    public void testBrokenMime() {
        assertResult("unknown type", "");
        assertResult("unknown type", "text");
        assertResult("unknown type", "bla/bla");
    }

    /**
     * Test of doesProcessNoValue method, of class FormatPostNormalizer.
     */
    @Test
    public void testDoesProcessNoValue() {
        assertFalse(instance.doesProcessNoValue());
    }

    public void assertResult(String expected, String value) {
        List<String> result = instance.process(value, null);
        assertNotNull(result);
        assertTrue(result.size() == 1);
        assertEquals(expected, result.get(0));
    }

}
