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
import eu.clarin.cmdi.vlo.pojo.DocumentField;
import eu.clarin.cmdi.vlo.service.FieldFilter;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.model.Model;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class DocumentFieldsProviderTest {

    private final Mockery context = new JUnit4Mockery();
    private DocumentFieldsProvider instance;
    private SolrDocument document;
    private FieldFilter filter;

    @Before
    public void setUp() {
        filter = context.mock(FieldFilter.class);

        document = new SolrDocument();
        document.addField("field1", "value1");
        document.addField("field2", "value2");
        document.addField("field3", "value3");
        document.addField("field4", "value4");

        instance = new DocumentFieldsProvider(Model.of(document), filter, null);
    }

    /**
     * Test of size method, of class DocumentFieldsProvider.
     */
    @Test
    public void testSize() {
        context.checking(new Expectations() {
            {
                oneOf(filter).allowField("field1");
                will(returnValue(true));

                // this mock filter disallows field 2
                oneOf(filter).allowField("field2");
                will(returnValue(false));

                oneOf(filter).allowField("field3");
                will(returnValue(true));

                oneOf(filter).allowField("field4");
                will(returnValue(true));
            }
        });
        // two items as field2 is excluded by the filter
        assertEquals(3, instance.size());
    }

    @Test
    public void testOrder() {
        // pass in a filter that allows all fields
        final FieldFilter allowAllFilter = new FieldFilter() {

            @Override
            public boolean allowField(String fieldName) {
                return true;
            }
        };

        // pass in an explicit field order
        final List<String> order = ImmutableList.of("field4", "field2", "field1", "field3");
        instance = new DocumentFieldsProvider(Model.of(document), allowAllFilter, order);

        final long first = 0L;
        final long count = 100L;
        final Iterator<? extends DocumentField> result = instance.iterator(first, count);
        assertTrue(result.hasNext());
        assertEquals("field4", result.next().getFieldName());
        assertTrue(result.hasNext());
        assertEquals("field2", result.next().getFieldName());
        assertTrue(result.hasNext());
        assertEquals("field1", result.next().getFieldName());
        assertTrue(result.hasNext());
        assertEquals("field3", result.next().getFieldName());
        assertFalse(result.hasNext());
    }

    /**
     * Test of iterator method, of class DocumentFieldsProvider.
     */
    @Test
    public void testIterator() {
        context.checking(new Expectations() {
            {
                oneOf(filter).allowField("field1");
                will(returnValue(true));

                // this mock filter disallows field 2
                oneOf(filter).allowField("field2");
                will(returnValue(false));

                oneOf(filter).allowField("field3");
                will(returnValue(true));

                oneOf(filter).allowField("field4");
                will(returnValue(false));
            }
        });

        final long first = 0L;
        final long count = 100L;
        final Iterator<? extends DocumentField> result = instance.iterator(first, count);
        assertTrue(result.hasNext());
        assertEquals("field1", result.next().getFieldName());
        assertTrue(result.hasNext());
        // field 2 filtered out
        assertEquals("field3", result.next().getFieldName());
        assertFalse(result.hasNext());
        // field 4 filtered out
    }

    @Test
    public void testIteratorOffset() {

        context.checking(new Expectations() {
            {
                oneOf(filter).allowField("field1");
                will(returnValue(true));

                // this mock filter disallows field 2
                oneOf(filter).allowField("field2");
                will(returnValue(false));

                oneOf(filter).allowField("field3");
                will(returnValue(true));

                oneOf(filter).allowField("field4");
                will(returnValue(true));
            }
        });

        // 3 fields in total (1, 3 and 4)
        assertEquals(3, instance.size());

        final long first = 1L;
        final long count = 1L;
        final Iterator<? extends DocumentField> result = instance.iterator(first, count);
        assertTrue(result.hasNext());
        // field 3 is the first field because of the offset of 1
        assertEquals("field3", result.next().getFieldName());
    }
}
