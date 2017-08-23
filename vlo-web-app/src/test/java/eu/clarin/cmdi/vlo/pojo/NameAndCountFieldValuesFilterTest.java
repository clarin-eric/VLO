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
        assertTrue(filter.matches(count, null));

        filter.setName("val");
        assertTrue(filter.matches(count, null));

        filter.setName("value");
        assertTrue(filter.matches(count, null));

        filter.setName("values");
        assertFalse(filter.matches(count, null));
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
        assertTrue(filter.matches(count, null));

        filter.setFirstCharacter('V');
        assertTrue(filter.matches(count, null));

        filter.setFirstCharacter('A');
        assertFalse(filter.matches(count, null));

        filter.setFirstCharacter('*');
        assertFalse(filter.matches(count, null));
    }

    /**
     * Test of matches method, of class NameAndCountFieldValuesFilter.
     */
    @Test
    public void testMatchesOtherCharacter() {
        filter.setFirstCharacter('*');
        assertFalse(filter.matches(count, null));
        
        count.setName("???");
        assertTrue(filter.matches(count, null));
    }

    /**
     * Test of isEmpty method, of class NameAndCountFieldValuesFilter.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(filter.isEmpty());

        filter.setName("value");
        assertFalse(filter.isEmpty());

        filter.setName(null);
        assertTrue(filter.isEmpty());
    }

}
