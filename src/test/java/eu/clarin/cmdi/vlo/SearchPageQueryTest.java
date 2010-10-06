package eu.clarin.cmdi.vlo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Test;

import eu.clarin.cmdi.vlo.pages.SearchPageQuery;

public class SearchPageQueryTest {

    @Test
    public void testQueryParse() throws Exception {
        String queryParam = "q=*:*&fl=name&facet=true&facet.field=cat&facet.field=inStock";
        SearchPageQuery q = new SearchPageQuery(queryParam);
        assertEquals("*:*", q.getSolrQuery().getQuery());
        assertEquals("name", q.getSolrQuery().getFields());
        assertEquals(2, q.getSolrQuery().getFacetFields().length);
        assertEquals("cat", q.getSolrQuery().getFacetFields()[0]);
        assertEquals("inStock", q.getSolrQuery().getFacetFields()[1]);
        assertNull(q.getSolrQuery().getFilterQueries());
        
        queryParam = "q=*:*&rows=10&start=0&fl=name,id&facet=true&facet.field=country&facet.field=genre&fq=country:New\\ Zealand";
        q = new SearchPageQuery(queryParam);
        assertEquals("*:*", q.getSolrQuery().getQuery());
        assertEquals("name,id", q.getSolrQuery().getFields());
        assertEquals(2, q.getSolrQuery().getFacetFields().length);
        assertEquals("country", q.getSolrQuery().getFacetFields()[0]);
        assertEquals("genre", q.getSolrQuery().getFacetFields()[1]);
        assertEquals(1, q.getSolrQuery().getFilterQueries().length);
        assertEquals("country:New\\ Zealand", q.getSolrQuery().getFilterQueries()[0]);
        assertEquals("New\\ Zealand", q.getSelectedValue(new FacetField("country")));
        assertNull(q.getSelectedValue(new FacetField("genre")));
    }
}
