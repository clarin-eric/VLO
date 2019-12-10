package eu.clarin.cmdi;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import eu.clarin.cmdi.vlo.exposure.frontend.HomePage;
import eu.clarin.cmdi.vlo.exposure.frontend.WicketApplication;
import org.junit.Ignore;


/**
 * Simple test using the WicketTester
 */
@Ignore
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
