package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.pages.SearchPageQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.params.CommonParams;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class SearchPageQueryTest {

    @Before
    public void setup() {
        
        // read the configuration from the packaged configuration file
        VloConfig.readPackagedConfig();

        // optionally, modify the configuration here
    }

    @Test
    public void testQueryParse() throws Exception {
        PageParameters params = new PageParameters();
        SearchPageQuery q = new SearchPageQuery(params);
        assertEquals("", q.getSearchQuery());
        assertEquals("*:*", q.getSolrQuery().getQuery());
        assertEquals("name,id,description", q.getSolrQuery().getFields());
        assertEquals(13, q.getSolrQuery().getFacetFields().length);
        assertEquals("collection", q.getSolrQuery().getFacetFields()[0]);
        assertEquals("resourceClass", q.getSolrQuery().getFacetFields()[2]);
        assertNull(q.getSolrQuery().getFilterQueries());

        params = new PageParameters();
        params.add(CommonParams.Q, "test");
        params.add(CommonParams.FQ, "country:New Zealand");
        q = new SearchPageQuery(params);
        assertEquals("test", q.getSearchQuery());
        assertEquals("test", q.getSolrQuery().getQuery());
        assertEquals("name,id,description", q.getSolrQuery().getFields());
        assertEquals(13, q.getSolrQuery().getFacetFields().length);
        assertEquals("collection", q.getSolrQuery().getFacetFields()[0]);
        assertEquals("resourceClass", q.getSolrQuery().getFacetFields()[2]);
        assertEquals(1, q.getSolrQuery().getFilterQueries().length);
        assertEquals("country:New\\ Zealand", q.getSolrQuery().getFilterQueries()[0]);
        assertEquals("New Zealand", q.getSelectedValue(new FacetField("country")));
        assertNull(q.getSelectedValue(new FacetField("genre")));
    }
}
