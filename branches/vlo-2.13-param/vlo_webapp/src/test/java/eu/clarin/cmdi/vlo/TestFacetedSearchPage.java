package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestFacetedSearchPage {
    
    static VloConfig testConfig;

    @Before
    public void setUp() {

        WicketTester wicketTester;

        String fileName = VloConfig.class.getResource("/VloConfig.xml").getFile();

        testConfig = VloConfig.readTestConfig(fileName);

        // optionally, modify the test configuration here

        wicketTester = new WicketTester(new VloApplication(testConfig));
    }

    @Test
    public void testRenderMyPage() {
//        tester.startPage(FacetedSearchPage.class);
//        tester.assertRenderedPage(FacetedSearchPage.class);
//        tester.assertLabel("message", "If you see this message wicket is properly configured and running");
    }
}
