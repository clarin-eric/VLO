package eu.clarin.cmdi.vlo;

import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;

import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;

/**
 * Simple test using the WicketTester
 */
public class TestFacetedSearchPage extends TestCase {
    private WicketTester tester;

    @Override
    public void setUp() {
        Configuration.getInstance().setSolrUrl("http://localhost:8080/vlo_solr");
        tester = new WicketTester(new VloApplication());
    }

    public void testRenderMyPage() {
        tester.startPage(FacetedSearchPage.class);
        tester.assertRenderedPage(FacetedSearchPage.class);
        //tester.assertLabel("message", "If you see this message wicket is properly configured and running");
    }
}
