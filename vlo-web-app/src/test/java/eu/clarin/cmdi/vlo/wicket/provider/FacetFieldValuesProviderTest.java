/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.provider;

import com.google.common.collect.ImmutableSet;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import java.util.Collection;
import java.util.Iterator;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class FacetFieldValuesProviderTest {

    public final static Collection<String> LOW_PRIORITY_VALUES = ImmutableSet.of("Xlow priority");

    private FacetField facetField;

    @Before
    public void setUp() {
        facetField = new FacetField("field");
        facetField.add("first value", 101);
        facetField.add("second value*", 102);
        facetField.add("Xlow priority", 500);
        facetField.add("third value", 103);
        facetField.add("FOURTH value", 104); //intentional upper case, sort and filter should be case insensitive
    }

    /**
     * Test of iterator method, of class FacetFieldValuesProvider.
     */
    @Test
    public void testIteratorCountOrder() {
        final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 10, LOW_PRIORITY_VALUES, new SortParam<FieldValuesOrder>(FieldValuesOrder.COUNT, true));

        long first = 0;
        long count = 100;
        final Iterator<? extends FacetField.Count> result = instance.iterator(first, count);

        //sorted by count
        assertTrue(result.hasNext());
        FacetField.Count valueCount = result.next();
        assertEquals(101, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(102, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(103, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(104, valueCount.getCount());

        // low priority last
        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(500, valueCount.getCount());

        assertFalse(result.hasNext());
    }

    /**
     * Test of iterator method, of class FacetFieldValuesProvider.
     */
    @Test
    public void testIteratorCountOrderDescending() {
        final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 10, LOW_PRIORITY_VALUES, new SortParam<FieldValuesOrder>(FieldValuesOrder.COUNT, false));

        long first = 0;
        long count = 100;
        final Iterator<? extends FacetField.Count> result = instance.iterator(first, count);

        //sorted by count (descending)
        assertTrue(result.hasNext());
        FacetField.Count valueCount = result.next();
        assertEquals(104, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(103, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(102, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(101, valueCount.getCount());

        // low priority last
        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(500, valueCount.getCount());

        assertFalse(result.hasNext());
    }

    /**
     * Test of iterator method, of class FacetFieldValuesProvider.
     */
    @Test
    public void testIteratorCountOrderDefaultPriority() {
        final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 10, new SortParam<FieldValuesOrder>(FieldValuesOrder.COUNT, false));

        long first = 0;
        long count = 100;
        final Iterator<? extends FacetField.Count> result = instance.iterator(first, count);

        //sorted by count (descending), no low priority defined
        assertTrue(result.hasNext());
        FacetField.Count valueCount = result.next();
        assertEquals(500, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(104, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(103, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(102, valueCount.getCount());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals(101, valueCount.getCount());

        assertFalse(result.hasNext());
    }

    /**
     * Test of iterator method, of class FacetFieldValuesProvider.
     */
    @Test
    public void testIteratorNameOrderDescending() {
        final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 10, LOW_PRIORITY_VALUES, new SortParam<FieldValuesOrder>(FieldValuesOrder.NAME, false));

        final long first = 0;
        final long count = 100;
        final Iterator<? extends FacetField.Count> result = instance.iterator(first, count);

        //sorted by name (descending)
        assertTrue(result.hasNext());
        FacetField.Count valueCount = result.next();
        assertEquals("Xlow priority", valueCount.getName()); // priority only affects sort by count

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals("third value", valueCount.getName());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals("second value*", valueCount.getName());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals("FOURTH value", valueCount.getName()); // case insensitive sorting

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals("first value", valueCount.getName());

        assertFalse(result.hasNext());
    }

    /**
     * Test of iterator method, of class FacetFieldValuesProvider.
     */
    @Test
    public void testIteratorOffset() {
        final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 10, LOW_PRIORITY_VALUES, new SortParam<FieldValuesOrder>(FieldValuesOrder.COUNT, true));

        final long first = 2;
        final long count = 100;
        final Iterator<? extends FacetField.Count> result = instance.iterator(first, count);

        //sorted by count, offset 2
        assertTrue(result.hasNext());
        FacetField.Count valueCount = result.next();
        assertEquals(103, valueCount.getCount());
    }

    /**
     * Test of size method, of class FacetFieldValuesProvider.
     */
    @Test
    public void testSize() {
        // potential is lower than limit
        {
            final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 10, LOW_PRIORITY_VALUES, new SortParam<FieldValuesOrder>(FieldValuesOrder.NAME, false));
            // actual number is returned
            assertEquals(5, instance.size());
        }
        // potential is higher than limit
        {
            final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 2, LOW_PRIORITY_VALUES, new SortParam<FieldValuesOrder>(FieldValuesOrder.NAME, false));
            // maximum number is returned (result is capped)
            assertEquals(2, instance.size());
        }
    }

    /**
     * Test of iterator method, of class FacetFieldValuesProvider.
     */
    @Test
    public void testFiltered() {
        final Model<FieldValuesFilter> filterModel = Model.of(new FieldValuesFilter());
        filterModel.getObject().setName("th");

        final FacetFieldValuesProvider instance = new FacetFieldValuesProvider(Model.of(facetField), 10, LOW_PRIORITY_VALUES, new SortParam<FieldValuesOrder>(FieldValuesOrder.NAME, true)) {

            @Override
            protected IModel<FieldValuesFilter> getFilterModel() {
                return filterModel;
            }

        };

        final long first = 0;
        final long count = 100;
        final Iterator<? extends FacetField.Count> result = instance.iterator(first, count);

        //sorted by name, filtered on presence of 'th'
        assertTrue(result.hasNext());
        FacetField.Count valueCount = result.next();
        assertEquals("FOURTH value", valueCount.getName());

        assertTrue(result.hasNext());
        valueCount = result.next();
        assertEquals("third value", valueCount.getName());

        assertFalse(result.hasNext());

        // add minimal occurences condition to filter
        filterModel.getObject().setMinimalOccurence(104);
        filterModel.getObject().setName(null);
        // re-evaluate - only 'FOURTH' and 'low priority' value should match
        assertEquals(2, instance.size());

        // test literal matching
        filterModel.getObject().setMinimalOccurence(0);
        filterModel.getObject().setName("*");
        // re-evaluate - only "second value*" value should match
        assertEquals(1, instance.size());
    }
}
