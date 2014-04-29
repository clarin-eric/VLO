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
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.solr.SolrFacetQueryFactory;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class SolrFacetFieldsServiceTest {

    private final Mockery context = new JUnit4Mockery();

    private SolrFacetQueryFactory queryFactory;
    private SearchResultsDao dao;
    private SolrFacetFieldsService instance;

    @Before
    public void setUp() {
        dao = context.mock(SearchResultsDao.class);
        queryFactory = context.mock(SolrFacetQueryFactory.class);
        instance = new SolrFacetFieldsService(dao, queryFactory);
    }

    /**
     * Test of getFacetFields method, of class SolrFacetFieldsService.
     */
    @Test
    public void testGetFacetFields() {
        // selection passed to service
        final QueryFacetsSelection selection = new QueryFacetsSelection("query");
        // query returned by factory for selection
        final SolrQuery query = new SolrQuery("query");
        // fields returned by dao for query
        final List<FacetField> fields = ImmutableList.of(new FacetField("field1"), new FacetField("field2"));
        final List<String> facets = ImmutableList.of("facet1", "facet2");

        context.checking(new Expectations() {
            {
                oneOf(queryFactory).createFacetQuery(selection, facets, 20);
                will(returnValue(query));
                oneOf(dao).getFacets(query);
                will(returnValue(fields));
            }
        });

        final List<FacetField> result = instance.getFacetFields(selection, facets, 20);
        assertEquals(fields, result);
    }

    /**
     * Test of getFacetFieldCount method, of class SolrFacetFieldsService.
     */
    @Test
    public void testGetFacetFieldCount() {
        // counting query returned by factory
        final SolrQuery query = new SolrQuery("query");
        // fields returned by dao for query
        final List<FacetField> fields = ImmutableList.of(new FacetField("field1"), new FacetField("field2"));
        final List<String> facets = ImmutableList.of("facet1", "facet2");

        context.checking(new Expectations() {
            {
                oneOf(queryFactory).createCountFacetsQuery(facets);
                will(returnValue(query));
                oneOf(dao).getFacets(query);
                will(returnValue(fields));
            }
        });

        final long result = instance.getFacetFieldCount(facets);
        assertEquals(2, result);
    }

}
