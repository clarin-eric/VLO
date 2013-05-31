package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.VloWebApplication;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.resource.ContextRelativeResource;

public class BasePage extends WebPage implements IHeaderContributor{
    
    public BasePage(PageParameters parameters) {
        super(parameters);
        
        // delete all parameters from the map, except theme
        String theme;
        theme = parameters.getKey("theme");
        if (theme == null){
            theme = "";
        }
        parameters = new PageParameters ();
        parameters.add("theme", theme);
        
        // set the applications (local) homelink to the faceted search page
                BookmarkablePageLink link = new BookmarkablePageLink("homeLink", 
                FacetedSearchPage.class, parameters);
        add(link);
               
        // refer to the the left part of the vlo banner as a resource
        Resource leftImageRes;
        leftImageRes = new ContextRelativeResource("Images/topleftvlo.gif");

        // create the image
        Image leftImage;
        leftImage = new Image("leftimage", leftImageRes);
 
        // add the image to the page
        link.add(leftImage);

        // refer to the right part of the vlo banner as a resource
        Resource rightImageRes;
        rightImageRes = new ContextRelativeResource("Images/toprightvlo.gif");
        
        // create the image
        Image rightImage;
        rightImage = new Image("rightimage", rightImageRes);
        
        // add it to the page
        add (rightImage);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
                
        response.renderCSSReference("css/main.css");
    }
    
}
