package eu.clarin.cmdi.vlo.exposure.frontend.test;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import eu.clarin.cmdi.vlo.exposure.frontend.HomePage;
import eu.clarin.cmdi.vlo.exposure.frontend.WicketApplication;


/**
 * Simple test using the WicketTester
 */
public class TestHomePage {
    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(new WicketApplication());
    }

    @Test
    public void homepageRendersSuccessfully() {
        // start and render the test page
        tester.startPage(HomePage.class);

        // assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }
}
