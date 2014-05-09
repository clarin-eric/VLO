package eu.clarin.cmdi.vlo;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestFacetedSearchPage {
    
    /**
     * Check Wicket with simple test taken from the WicketTester Javadoc
     */
    @Test
    //test code
    public void testRenderYourPage() {

        class PageWithLink extends WebPage {

            public PageWithLink() {
                add(new Label("myMessage", "Hello!"));
                add(new Link("toYourPage") {
                    @Override
                    public void onClick() {
                        setResponsePage(new LinkedPage("Hi!"));
                    }
                });
            }

            class LinkedPage extends WebPage {

                public LinkedPage(String message) {
                    add(new Label("yourMessage", message));
                    info("Wicket Rocks ;-)");
                }
            }
        }
    }
}
