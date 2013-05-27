package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloWebApplication;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class BasePage extends WebPage {
   
    public BasePage(PageParameters parameters) {
        super(parameters);
        
        // check for the theme parameter
        VloWebApplication app = (VloWebApplication) this.getApplication();
        String theme = app.getTheme();

        if (theme == null) {
            // client did not specify a theme parameter
        } else {
            // add the theme parameter
            parameters.add("theme", theme);
            
            // determine the intended css and "install" it
            // determine the intended picture and install it
            // this might not have to be done for every page
        }
        
        add(new BookmarkablePageLink("homeLink", FacetedSearchPage.class));
    }    
}
