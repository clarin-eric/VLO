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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import java.util.List;
import static org.apache.commons.lang3.CharSetUtils.count;
import static org.apache.solr.SolrJettyTestBase.context;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class PartitionedDataProviderTest {

    private final Mockery context = new JUnit4Mockery();

    /**
     * Test of size method, of class PartitionedDataProvider.
     */
    @Test
    public void testSizeNoPartitions() {

        final ISortableDataProvider provider = context.mock(ISortableDataProvider.class);
        // create instance with no partitioning (one list item)
        final PartitionedDataProvider instance = new PartitionedDataProvider(provider, 0);

        context.checking(new Expectations() {
            {
                never(provider).size(); //should not be called, there is always 1 partition
            }
        });

        assertEquals(1, instance.size());
    }

    /**
     * Test of iterator method, of class PartitionedDataProvider.
     */
    @Test
    public void testSizePartitions() {

        final ISortableDataProvider provider = context.mock(ISortableDataProvider.class);
        // create instance with partitions of 2
        final PartitionedDataProvider instance = new PartitionedDataProvider(provider, 2);

        context.checking(new Expectations() {
            {
                oneOf(provider).size();
                will(returnValue(9L));
            }
        });

        assertEquals("ceil(9/2) = 5", 5, instance.size());
    }

    /**
     * Test of iterator method, of class PartitionedDataProvider.
     */
    @Test
    public void testIteratorNoPartitions() {

        final ISortableDataProvider provider = context.mock(ISortableDataProvider.class);
        // create instance with no partitioning (one list item)
        final PartitionedDataProvider instance = new PartitionedDataProvider(provider, 0);
        final Iterator<String> result = Iterators.forArray("value 1", "value 2", "value 3");

        context.checking(new Expectations() {
            {
                oneOf(provider).iterator(0, Long.MAX_VALUE);
                will(returnValue(result));
            }
        });

        final Iterator iterator = instance.iterator(0, 100);
        assertTrue(iterator.hasNext());

        final Object object = iterator.next();
        assertTrue(object instanceof List);

        final List<String> list = (List) object;
        assertEquals(3, list.size());
        assertThat(list, hasItems("value 1", "value 2", "value 3"));

        // only one list item
        assertFalse(iterator.hasNext());
    }

    /**
     * Test of iterator method, of class PartitionedDataProvider.
     */
    @Test
    public void testIteratorPartitions() {

        final ISortableDataProvider provider = context.mock(ISortableDataProvider.class);
        // create instance with partitions of 2
        final PartitionedDataProvider instance = new PartitionedDataProvider(provider, 2);
        // five values, expecting 3 partitions (2-2-1)
        final Iterator<String> result = Iterators.forArray("value 1", "value 2", "value 3", "value 4", "value 5");

        context.checking(new Expectations() {
            {
                oneOf(provider).iterator(0, 10); // count 5 partitions -> count 10 values
                will(returnValue(result));
            }
        });

        final Iterator iterator = instance.iterator(0, 5);
        {
            assertTrue(iterator.hasNext());

            final Object object = iterator.next();
            assertTrue(object instanceof List);

            final List<String> list = (List) object;
            assertEquals(2, list.size());
            assertThat(list, hasItems("value 1", "value 2"));
        }
        {
            assertTrue(iterator.hasNext());
            final List<String> list = (List) iterator.next();
            assertEquals(2, list.size());
            assertThat(list, hasItems("value 3", "value 4"));
        }
        {
            // last list item, one value remaining 
            assertTrue(iterator.hasNext());
            final List<String> list = (List) iterator.next();
            assertEquals(1, list.size());
            assertThat(list, hasItems("value 5"));
        }

        // no further list item
        assertFalse(iterator.hasNext());
    }

    /**
     * Test of detach method, of class PartitionedDataProvider.
     */
    @Test
    public void testDetach() {
        final ISortableDataProvider provider = context.mock(ISortableDataProvider.class);
        // create instance with partitions of 2
        final PartitionedDataProvider instance = new PartitionedDataProvider(provider, 2);
        context.checking(new Expectations() {
            {
                oneOf(provider).detach();
            }
        });
        instance.detach();
    }

    /**
     * Test of getSortState method, of class PartitionedDataProvider.
     */
    @Test
    public void testGetSortState() {
        final ISortableDataProvider provider = context.mock(ISortableDataProvider.class);
        // create instance with partitions of 2
        final PartitionedDataProvider instance = new PartitionedDataProvider(provider, 2);
        final ISortState sortState = context.mock(ISortState.class);
        context.checking(new Expectations() {
            {
                oneOf(provider).getSortState();
                will(returnValue(sortState));
            }
        });
        assertSame(sortState, instance.getSortState());
    }

}
