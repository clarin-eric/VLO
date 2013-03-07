package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestFacetedSearchPage {

    @Before
    public void setUp() {

        WicketTester wicketTester;

        // read the packaged configuration 
        VloConfig.readPackagedConfig();

        // optionally, modify the configuration here

        wicketTester = new WicketTester();
    }

    @Test
    public void testRenderMyPage() {
//        tester.startPage(FacetedSearchPage.class);
//        tester.assertRenderedPage(FacetedSearchPage.class);
//        tester.assertLabel("message", "If you see this message wicket is properly configured and running");
    }
}
