
package eu.clarin.cmdi.vlo.pages;

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.HashMap;
import java.util.Map;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Panel showing a resource link. 
 * 
 * In the extension of the link panel class, this class adds a label text and 
 * an icon that is specific for search page links.
 */
public class ResourceLinkPanel extends LinkPanel {

    private final static Logger LOG = 
            LoggerFactory.getLogger(ResourceLinkPanel.class);

    private static final long serialVersionUID = 1L;

    private final static String URN_NBN_RESOLVER_URL = 
            "http://www.nbn-resolving.org/redirect/";

    private final static ImageResource ANNOTATION = new ImageResource("text-x-log.png", "Annotation file");
    private final static ImageResource AUDIO = new ImageResource("audio-x-generic.png", "Audio file");
    private final static ImageResource IMAGE = new ImageResource("image-x-generic.png", "Image file");
    private final static ImageResource TEXT = new  ImageResource("text-x-generic.png", "Text file");
    private final static ImageResource VIDEO = new ImageResource("video-x-generic.png", "Video file");

    private final static Map<String, ImageResource> ICON_MAP = new 
            HashMap<String, ImageResource>();
    static {
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_AUDIO, AUDIO);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_VIDEO, VIDEO);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_TEXT, TEXT);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_IMAGE, IMAGE);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_ANNOTATION, ANNOTATION);
    }
    
    /** 
     * Panel constructor
     * 
     * @param id Wicket mark up identifier
     * @param mimeType mime type of the resource indicated
     * @param resourceLink URL to pointing to the resource
     */
    public ResourceLinkPanel(String id, String mimeType, String resourceLink) {
        super(id);
        ImageResource imageResouce = getImage(mimeType);
        Image resourceImg = new Image("resourceImage", imageResouce.getResource());
        String title = imageResouce.getTitle() + " (" + mimeType + ")";
        resourceImg.add(new AttributeModifier("title", title));
        resourceImg.add(new AttributeModifier("alt", title));
        String href = getHref(resourceLink);
        String name = getNameFromLink(resourceLink);
        ExternalLink link = new ExternalLink("resourceLink", href);
        link.add(resourceImg);
        link.add(new Label("resourceLabel", name));
        add(link);
    }
    
    private ImageResource getImage(String mimeType) {
        ImageResource image = ICON_MAP.get(CommonUtils.normalizeMimeType(mimeType));
        if (image == null) {
            image = TEXT; //unknow defaults to TEXT
        }
        return image;
    }
}
