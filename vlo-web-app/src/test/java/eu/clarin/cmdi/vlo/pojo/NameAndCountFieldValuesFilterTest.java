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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author twagoo
 */
public class NameAndCountFieldValuesFilterTest {

    private Count count;
    private NameAndCountFieldValuesFilter filter;

    @BeforeEach
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
        assertTrue(filter.matches(count, null), "Partial left match");

        filter.setName("val");
        assertTrue(filter.matches(count, null), "Partial left match");

        filter.setName("value");
        assertTrue(filter.matches(count, null), "Complete match");

        filter.setName("alue");
        assertTrue(filter.matches(count, null), "Partial middle match");

        filter.setName("values");
        assertFalse(filter.matches(count, null), "Complete match");
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
        assertTrue(filter.matches(count, null), "Lower case");

        filter.setFirstCharacter('V');
        assertTrue(filter.matches(count, null), "Upper case");

        filter.setFirstCharacter('A');
        assertFalse(filter.matches(count, null), "Mismatch (alphabetical)");

        filter.setFirstCharacter('?');
        assertFalse(filter.matches(count, null), "Mismatch (other char)");

        filter.setFirstCharacter(NameAndCountFieldValuesFilter.ANY_CHARACTER_SYMBOL);
        assertTrue(filter.matches(count, null), "Any character for non-empty string");
        count.setName("");
        assertFalse(filter.matches(count, null), "Any character for empty string");
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
        assertFalse(filter.isEmpty(), "Name filter");

        filter.setName(null);
        assertTrue(filter.isEmpty());

        filter.setMinimalOccurence(10);
        assertFalse(filter.isEmpty(), "Count filter");

        filter.setMinimalOccurence(null);
        assertTrue(filter.isEmpty());

        filter.setFirstCharacter('c');
        assertFalse(filter.isEmpty(), "Character filter");

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
