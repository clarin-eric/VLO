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

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.service.solr.impl.SolrDocumentServiceImpl;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.SearchResultsDao;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentQueryFactory;

import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jmock.Expectations;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */

public class SolrDocumentServiceImplTest {

    private final Mockery context = new JUnit4Mockery();
    private SolrDocumentServiceImpl instance;
    private SearchResultsDao dao;
    private SolrDocumentQueryFactory queryFactory;

    @Before
    public void setUp() {
        dao = context.mock(SearchResultsDao.class);
        queryFactory = context.mock(SolrDocumentQueryFactory.class);
        instance = new SolrDocumentServiceImpl(dao, queryFactory);
    }

    /**
     * Test of getDocument method, of class SolrDocumentServiceImpl.
     */
    @Test
    public void testGetDocument() {
        final SolrQuery solrQuery = new SolrQuery("query");
        final SolrDocument resultDocument = new SolrDocument();
        resultDocument.setField("id", "documentId");
        final SolrDocumentList resultList = new SolrDocumentList();
        resultList.add(resultDocument);
        context.checking(new Expectations() {
            {
                oneOf(queryFactory).createDocumentQuery("documentId");
                will(returnValue(solrQuery));
                oneOf(dao).getDocuments(solrQuery);
                will(returnValue(resultList));
            }
        });

        final SolrDocument result = instance.getDocument("documentId");
        assertEquals(resultDocument, result);
        assertEquals("documentId", result.getFieldValue("id"));
    }

    /**
     * Test of getDocuments method, of class SolrDocumentServiceImpl.
     */
    @Test
    public void testGetDocuments() {
        final QueryFacetsSelection selection = new QueryFacetsSelection("query", Collections.<String, FacetSelection>emptyMap());
        final int first = 100;
        final int count = 15;

        final SolrQuery solrQuery = new SolrQuery("query");
        final SolrDocumentList resultList = new SolrDocumentList();
        resultList.add(new SolrDocument());
        resultList.add(new SolrDocument());

        context.checking(new Expectations() {
            {
                oneOf(queryFactory).createDocumentQuery(selection, first, count);
                will(returnValue(solrQuery));
                oneOf(dao).getDocuments(solrQuery);
                will(returnValue(resultList));
            }
        });

        final List<SolrDocument> result = instance.getDocuments(selection, first, count);
        assertEquals(resultList, result);
    }

    /**
     * Test of getDocumentCount method, of class SolrDocumentServiceImpl.
     */
    @Test
    public void testGetDocumentCount() {
        final QueryFacetsSelection selection = new QueryFacetsSelection("query", Collections.<String, FacetSelection>emptyMap());

        final SolrQuery solrQuery = new SolrQuery("query");
        final SolrDocumentList resultList = new SolrDocumentList();
        resultList.setNumFound(42);

        context.checking(new Expectations() {
            {
                oneOf(queryFactory).createDocumentQuery(with(equal(selection)), with(any(Integer.class)), with(any(Integer.class)));
                will(returnValue(solrQuery));
                oneOf(dao).getDocuments(solrQuery);
                will(returnValue(resultList));
            }
        });
        final long result = instance.getDocumentCount(selection);
        assertEquals(42, result);
    }

}
