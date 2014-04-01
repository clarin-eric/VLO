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

import eu.clarin.cmdi.vlo.service.solr.impl.SolrFacetQueryFactoryImpl;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrQuery;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class SolrFacetQueryFactoryImplTest {

    public static final List<String> FACET_FIELDS = Arrays.asList("facet1", "facet2", "facet3");

    /**
     * static because it doesn't carry state
     */
    private static SolrFacetQueryFactoryImpl instance;

    @BeforeClass
    public static void setUpClass() {
        instance = new SolrFacetQueryFactoryImpl(FACET_FIELDS);
    }

    /**
     * Test of createFacetQuery method, of class SolrQueryFactoryImpl.
     */
    @Test
    public void testCreateFacetQueryNoFacets() {
        // default constructor -> empty
        QueryFacetsSelection selection = new QueryFacetsSelection();
        SolrQuery query = instance.createFacetQuery(selection, 20);

        // default: query selects all values 
        assertEquals("*:*", query.getQuery());

        // no selection -> no filter queries
        assertEquals(0, query.getFilterQueries().length);

        assertEquals(20, query.getFacetLimit());
    }

    /**
     * Test of createFacetQuery method, of class SolrQueryFactoryImpl.
     */
    @Test
    public void testCreateFacetQueryNoSelection() {
        // Facets are present but no values are selected
        Map<String, Collection<String>> selection = new HashMap<String, Collection<String>>() {
            {
                put("facet1", Collections.<String>emptySet());
                put("facet2", Collections.<String>emptyList());
            }
        };

        SolrQuery query = instance.createFacetQuery(new QueryFacetsSelection(selection), 20);

        // default: query selects all values 
        assertEquals("*:*", query.getQuery());

        // Only empty selections -> no filter queries
        assertEquals(0, query.getFilterQueries().length);
        
        // Facet limit should be adopted
        assertEquals(20, query.getFacetLimit());
    }

    /**
     * Test of createFacetQuery method, of class SolrQueryFactoryImpl.
     */
    @Test
    public void testCreateFacetQuerySelection() {
        // Some facets have one or more values selected
        Map<String, Collection<String>> selection = new HashMap<String, Collection<String>>() {
            {
                put("facet1", Arrays.asList("valueA"));
                put("facet2", Arrays.asList("valueB", "valueC"));
                put("facet3", Collections.<String>emptyList());
            }
        };
        SolrQuery query = instance.createFacetQuery(new QueryFacetsSelection(selection), 20);

        // default: query selects all values 
        assertEquals("*:*", query.getQuery());

        // Expecting three filter queries as three values have been selected in total
        assertEquals(3, query.getFilterQueries().length);
        assertThat(Arrays.asList(query.getFilterQueries()), Matchers.<String>hasItem("facet1:valueA"));
        assertThat(Arrays.asList(query.getFilterQueries()), Matchers.<String>hasItem("facet2:valueB"));
        assertThat(Arrays.asList(query.getFilterQueries()), Matchers.<String>hasItem("facet2:valueC"));
        // facet 3 does not occur as there is not selected value!
        
        // Facet limit should be adopted
        assertEquals(20, query.getFacetLimit());
    }

    /**
     * Test of createFacetQuery method, of class SolrQueryFactoryImpl.
     */
    @Test
    public void testCreateFacetQuerySelectionAndQuery() {
        Map<String, Collection<String>> selection = new HashMap<String, Collection<String>>() {
            {
                put("facet1", Arrays.asList("valueA"));
            }
        };
        SolrQuery query = instance.createFacetQuery(new QueryFacetsSelection("query string", selection), 20);

        assertEquals(1, query.getFilterQueries().length);
        assertEquals("query\\ string", query.getQuery()); //space should be escaped!

        // Expecting three filter queries as three values have been selected in total
        assertEquals(1, query.getFilterQueries().length);
        assertThat(Arrays.asList(query.getFilterQueries()), Matchers.<String>hasItem("facet1:valueA"));

        // Facet limit should be adopted
        assertEquals(20, query.getFacetLimit());
    }

    /**
     * Test of createCountFacetsQuery method, of class SolrQueryFactoryImpl.
     */
    @Test
    public void testCreateCountFacetsQuery() {
        SolrQuery query = instance.createCountFacetsQuery();
        assertArrayEquals(FACET_FIELDS.toArray(), query.getFacetFields());
    }

}
