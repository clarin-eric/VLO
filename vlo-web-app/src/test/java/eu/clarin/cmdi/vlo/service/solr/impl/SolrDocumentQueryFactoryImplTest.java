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
package eu.clarin.cmdi.vlo.service.solr.impl;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class SolrDocumentQueryFactoryImplTest {

    private final Collection<String> docFields = ImmutableList.of("field1", "field2", "field3");
    private SolrDocumentQueryFactoryImpl instance;

    @Before
    public void setUp() {
        instance = new SolrDocumentQueryFactoryImpl(docFields);
    }

    /**
     * Test of createDocumentQuery method, of class
     * SolrDocumentQueryFactoryImpl.
     */
    @Test
    public void testCreateDocumentQueryForSelection() {
        final Map<String, FacetSelection> selectionMap = Collections.<String, FacetSelection>singletonMap("field1", new FacetSelection(Collections.singleton("value 1")));
        final QueryFacetsSelection selection = new QueryFacetsSelection("query", selectionMap);
        final int first = 100;
        final int count = 15;

        final SolrQuery query = instance.createDocumentQuery(selection, first, count);

        assertEquals(Integer.valueOf(100), query.getStart());
        assertEquals(Integer.valueOf(15), query.getRows());

        final String[] filterQueries = query.getFilterQueries();
        assertEquals(1, filterQueries.length);
        assertEquals("field1:\"value\\ 1\"", filterQueries[0]);

        final String fields = query.getFields();
        assertTrue(fields.contains("field1"));
        assertTrue(fields.contains("field2"));
        assertTrue(fields.contains("field3"));
        assertEquals("query", query.getQuery());
    }

    /**
     * Test of createDocumentQuery method, of class
     * SolrDocumentQueryFactoryImpl.
     */
    @Test
    public void testCreateDocumentQueryForDocId() {
        final SolrQuery query = instance.createDocumentQuery("document\"Id");

        final String[] filterQueries = query.getFilterQueries();
        assertEquals(1, filterQueries.length);
        // expecting query that looks in both id and selflink fields with properly escaped values
        assertEquals("id:\"document\\\"Id\" OR _selfLink:\"document\\\"Id\"", filterQueries[0]);

        final String fields = query.getFields();
        assertTrue(fields.contains("field1"));
        assertTrue(fields.contains("field2"));
        assertTrue(fields.contains("field3"));
    }

}
