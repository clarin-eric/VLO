package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloWebApplication;
import eu.clarin.cmdi.vlo.VloSession;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.resource.ContextRelativeResource;

/**
 * Properties common to all VLO web application's page objects
 * 
 * @author keeloo
 */
public class BasePage extends WebPage implements IHeaderContributor{
    
    // reference to the web application object
    static VloWebApplication webApp;

    /**
     * Make sure every web application object sends this message
     *
     * @param vloWebApplication reference to the web application object
     */
    public static void setWebApp(VloWebApplication vloWebApplication) {
        webApp = vloWebApplication;
    }
    
    /**
     * Install a VLO theme<br><br>
     * 
     * A VLO theme is determined by a page title, a CSS file, and a banner split
     * in a left and right image.
     * 
     * The left part of the banner serves as a link to the faceted search page,
     * the application's start page. In the field below banner there is a link
     * to the page the web application is launched from; the applications home
     * page. This link page is defined in the VloConfig file.
     *     
     * @param parameters 
     */
    public BasePage(PageParameters parameters) {

        super(parameters);
        
        // set the page title
        
        Label pageTitle;
        pageTitle = new Label ("pagetitle", VloSession.get().getCurrentTheme().pageTitle);
        add (pageTitle);
        
        // set the applications start page link to the faceted search page
        PageParameters startPageParameters = new PageParameters ();
        // add the session persistent parameters
        startPageParameters.mergeWith(VloSession.get().getVloSessionPageParameters());

        BookmarkablePageLink link = new BookmarkablePageLink("startpage",
                FacetedSearchPage.class, startPageParameters);
        add(link);
                   
        // refer to the the left part of the vlo banner as a resource
        ContextRelativeResource leftImageRes;
        leftImageRes = new ContextRelativeResource(VloSession.get().getCurrentTheme().topLeftImage);

        // create the image
        Image leftImage;
        leftImage = new Image("leftimage", leftImageRes);
 
        // add the image to the page
        link.add(leftImage);

        // refer to the right part of the vlo banner as a resource
        ContextRelativeResource rightImageRes;
        rightImageRes = new ContextRelativeResource(VloSession.get().getCurrentTheme().topRightImage);
        
        // create the image
        Image rightImage;
        rightImage = new Image("rightimage", rightImageRes);
        
        // add it to the page
        add (rightImage);
        
        // set the partnerlinks
        
        Label partnerLinkMap;
        partnerLinkMap = new Label ("partnerlinkmap", VloSession.get().getCurrentTheme().partnerLinkMap);
        partnerLinkMap.setEscapeModelStrings(false);
        add (partnerLinkMap);
    }

    /**
     * Include the theme's CSS file in the HTML page<br><br>
     *
     * This method is invoked when Wicket renders a VLO page.
     *
     * @param response
     */
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forUrl(VloSession.get().getCurrentTheme().cssFile));
    }    
}
