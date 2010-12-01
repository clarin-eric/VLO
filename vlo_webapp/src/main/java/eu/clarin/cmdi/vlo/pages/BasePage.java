package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class BasePage extends WebPage {

    public BasePage(PageParameters parameters) {
        super(parameters);

        add(new BookmarkablePageLink("homeLink", FacetedSearchPage.class));
    }
}
