package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestFacetedSearchPage {
    
    // application configuration
    static VloConfig config;

    @Before
    public void setUp() {

        WicketTester wicketTester;

        // include the full path in the name of the packaged configuration file
        String fileName = VloConfig.class.getResource("/VloConfig.xml").getFile();

        // read the configuration defined in the file
        config = VloConfig.readTestConfig(fileName);

        // optionally, modify the test configuration here

        wicketTester = new WicketTester(new VloWebApplication(config));
    }

    @Test
    public void testRenderMyPage() {
//        tester.startPage(FacetedSearchPage.class);
//        tester.assertRenderedPage(FacetedSearchPage.class);
//        tester.assertLabel("message", "If you see this message wicket is properly configured and running");
    }
}
