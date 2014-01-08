
package eu.clarin.cmdi.vlo.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;

/**
 * Panel showing a landing page link. 
 * 
 * In the extension of the link panel class, this class adds a label text and 
 * an icon that is specific for search page links
 * 
 * @author keeloo
 */
public class LandingPageLinkPanel extends LinkPanel {

    private final static ImageResource LANDINGPAGE_ICON =
            new ImageResource(
            "Crystal_Clear_mimetype_readme.png",
            "Landing page");

    /**
     * Panel constructor. 
     * 
     * @param id Wicket mark up identifier
     * @param resourceLink URL to pointing to the resource.
     */
    public LandingPageLinkPanel(String id, String resourceLink) {
        // ...
        super(id);
        
        // add the icon image
        ImageResource imageResource = LANDINGPAGE_ICON;
        Image resourceImg = new Image("landingPageImage", imageResource.getResource());
        
        // add the image's title
        String title;
        title = imageResource.getTitle();
        resourceImg.add(new AttributeModifier("title", title));
        resourceImg.add(new AttributeModifier("alt", title));
        
        // ...
        String href = getHref(resourceLink);
        
        // get the name associated with the link
        String name = getNameFromLink(resourceLink);

        // ... and add the link itself
        ExternalLink link = new ExternalLink("landingPageLink", href);
        link.add(resourceImg);
        link.add(new Label("landingPageLabel", name));
        add(link);
    }
}
