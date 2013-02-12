package eu.clarin.cmdi.vlo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.params.CommonParams;
import org.apache.wicket.PageParameters;
import org.junit.Before;
import org.junit.Test;

import eu.clarin.cmdi.vlo.pages.SearchPageQuery;

public class SearchPageQueryTest {

    @Before
    public void setup() {
        // Configuration.getInstance().setFacetFields(new String[] { "collection", "continent", "organisation", "genre", "country", "language" });
    }
    
    @Test
    public void testQueryParse() throws Exception {
        PageParameters params = new PageParameters();
        SearchPageQuery q = new SearchPageQuery(params);
        assertEquals("", q.getSearchQuery());
        assertEquals("*:*", q.getSolrQuery().getQuery());
        assertEquals("name,id,description", q.getSolrQuery().getFields());
        assertEquals(6, q.getSolrQuery().getFacetFields().length);
        assertEquals("collection", q.getSolrQuery().getFacetFields()[0]);
        assertEquals("continent", q.getSolrQuery().getFacetFields()[1]);
        assertNull(q.getSolrQuery().getFilterQueries());

        params = new PageParameters();
        params.add(CommonParams.Q, "test");
        params.add(CommonParams.FQ, "country:New Zealand");
        q = new SearchPageQuery(params);
        assertEquals("test", q.getSearchQuery());
        assertEquals("test", q.getSolrQuery().getQuery());
        assertEquals("name,id,description", q.getSolrQuery().getFields());
        assertEquals(6, q.getSolrQuery().getFacetFields().length);
        assertEquals("collection", q.getSolrQuery().getFacetFields()[0]);
        assertEquals("continent", q.getSolrQuery().getFacetFields()[1]);
        assertEquals(1, q.getSolrQuery().getFilterQueries().length);
        assertEquals("country:New\\ Zealand", q.getSolrQuery().getFilterQueries()[0]);
        assertEquals("New Zealand", q.getSelectedValue(new FacetField("country")));
        assertNull(q.getSelectedValue(new FacetField("genre")));
    }
}
