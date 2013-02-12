package eu.clarin.cmdi.vlo;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestFacetedSearchPage {
    private WicketTester tester;

    @Before
    public void setUp() {
        // Configuration.getInstance().setSolrUrl("http://localhost:8080/vlo_solr");
        tester = new WicketTester(new VloApplication());
    }

    @Test
    public void testRenderMyPage() {
//        tester.startPage(FacetedSearchPage.class);
//        tester.assertRenderedPage(FacetedSearchPage.class);
//        tester.assertLabel("message", "If you see this message wicket is properly configured and running");
    }
}
