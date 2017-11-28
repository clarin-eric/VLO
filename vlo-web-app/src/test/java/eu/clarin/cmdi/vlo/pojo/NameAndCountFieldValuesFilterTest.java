/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo.pojo;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class NameAndCountFieldValuesFilterTest {

    private Count count;
    private NameAndCountFieldValuesFilter filter;

    @Before
    public void setUp() {
        filter = new NameAndCountFieldValuesFilter();
        count = new FacetField.Count(new FacetField("field"), "value", 0);
    }

    /**
     * Test of matches method, of class NameAndCountFieldValuesFilter.
     */
    @Test
    public void testMatchesName() {
        //initial state should match
        assertTrue(filter.matches(count, null));

        filter.setName(null);
        assertTrue(filter.matches(count, null));

        filter.setName("v");
        assertTrue("Partial left match", filter.matches(count, null));

        filter.setName("val");
        assertTrue("Partial left match", filter.matches(count, null));

        filter.setName("value");
        assertTrue("Complete match", filter.matches(count, null));

        filter.setName("alue");
        assertTrue("Partial middle match", filter.matches(count, null));

        filter.setName("values");
        assertFalse("Complete match", filter.matches(count, null));
    }

    /**
     * Test of matches method, of class NameAndCountFieldValuesFilter.
     */
    @Test
    public void testMatchesCount() {
        count.setCount(10);

        //initial state should match
        assertTrue(filter.matches(count, null));

        filter.setMinimalOccurence(null);
        assertTrue(filter.matches(count, null));

        filter.setMinimalOccurence(0);
        assertTrue(filter.matches(count, null));

        filter.setMinimalOccurence(10);
        assertTrue(filter.matches(count, null));

        filter.setMinimalOccurence(11);
        assertFalse(filter.matches(count, null));
    }

    /**
     * Test of matches method, of class NameAndCountFieldValuesFilter.
     */
    @Test
    public void testMatchesCharacter() {
        //initial state should match
        assertTrue(filter.matches(count, null));

        filter.setFirstCharacter(null);
        assertTrue(filter.matches(count, null));

        filter.setFirstCharacter('v');
        assertTrue("Lower case", filter.matches(count, null));

        filter.setFirstCharacter('V');
        assertTrue("Upper case", filter.matches(count, null));

        filter.setFirstCharacter('A');
        assertFalse("Mismatch (alphabetical)", filter.matches(count, null));

        filter.setFirstCharacter('?');
        assertFalse("Mismatch (other char)", filter.matches(count, null));

        filter.setFirstCharacter(NameAndCountFieldValuesFilter.ANY_CHARACTER_SYMBOL);
        assertTrue("Any character for non-empty string", filter.matches(count, null));
        count.setName("");
        assertFalse("Any character for empty string", filter.matches(count, null));
    }

    /**
     * Test of matches method, of class NameAndCountFieldValuesFilter.
     */
    @Test
    public void testMatchesOtherCharacter() {
        filter.setFirstCharacter(NameAndCountFieldValuesFilter.NON_ALPHABETICAL_CHARACTER_SYMBOL);
        assertFalse(filter.matches(count, null));

        count.setName("abc");
        assertFalse(filter.matches(count, null));

        count.setName("???");
        assertTrue(filter.matches(count, null));

        count.setName("543");
        assertTrue(filter.matches(count, null));

        count.setName("_other");
        assertTrue(filter.matches(count, null));
    }

    /**
     * Test of isEmpty method, of class NameAndCountFieldValuesFilter.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(filter.isEmpty());

        filter.setName("value");
        assertFalse("Name filter", filter.isEmpty());

        filter.setName(null);
        assertTrue(filter.isEmpty());

        filter.setMinimalOccurence(10);
        assertFalse("Count filter", filter.isEmpty());

        filter.setMinimalOccurence(null);
        assertTrue(filter.isEmpty());

        filter.setFirstCharacter('c');
        assertFalse("Character filter", filter.isEmpty());

        filter.setFirstCharacter(null);
        assertTrue(filter.isEmpty());
    }

    @Test
    public void testCopy() {
        filter.setName("origName");
        filter.setMinimalOccurence(999);
        filter.setFirstCharacter('a');
        final NameAndCountFieldValuesFilter copy = filter.copy();
        filter.setName("newName");
        filter.setMinimalOccurence(null);
        filter.setFirstCharacter(null);

        assertEquals("origName", copy.getName());
        assertEquals(Integer.valueOf(999), copy.getMinimalOccurence());
        assertEquals(Character.valueOf('a'), copy.getFirstCharacter());
    }

}
