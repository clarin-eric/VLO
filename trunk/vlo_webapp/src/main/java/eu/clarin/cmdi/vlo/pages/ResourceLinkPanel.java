package eu.clarin.cmdi.vlo.pages;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.FacetConstants;

public class ResourceLinkPanel extends Panel {
    private final static Logger LOG = LoggerFactory.getLogger(ResourceLinkPanel.class);

    private static final long serialVersionUID = 1L;

    private final static ImageResource ANNOTATION = new ImageResource(new ContextRelativeResource("Images/text-x-log.png"),
            "Annotation file");
    private final static ImageResource AUDIO = new ImageResource(new ContextRelativeResource("Images/audio-x-generic.png"), "Audio file");
    private final static ImageResource IMAGE = new ImageResource(new ContextRelativeResource("Images/image-x-generic.png"), "Image file");
    private final static ImageResource TEXT = new ImageResource(new ContextRelativeResource("Images/text-x-generic.png"), "Text file");
    private final static ImageResource VIDEO = new ImageResource(new ContextRelativeResource("Images/video-x-generic.png"), "Video file");
    private static final String HANDLE_PREFIX = "hdl";

    private final static Map<String, ImageResource> ICON_MAP = new HashMap<String, ImageResource>();
    static {
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_AUDIO, AUDIO);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_VIDEO, VIDEO);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_TEXT, TEXT);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_IMAGE, IMAGE);
        ICON_MAP.put(FacetConstants.RESOURCE_TYPE_ANNOTATION, ANNOTATION);
    }

    public ResourceLinkPanel(String id, String mimeType, String resourceLink) {
        super(id);
        ImageResource imageResouce = getImage(mimeType);
        Image resourceImg = new Image("resourceImage", imageResouce.getResource());
        String title = imageResouce.getTitle() + " (" + mimeType + ")";
        resourceImg.add(new SimpleAttributeModifier("title", title));
        resourceImg.add(new SimpleAttributeModifier("alt", title));
        add(resourceImg);
        String href = getHref(resourceLink);
        add(new ExternalLink("resourceLink", href, resourceLink));
    }

    private String getHref(String resourceLink) {
        String result = resourceLink;
        try {
            URL url = new URL(resourceLink);
            String protocol = url.getProtocol();
            if (HANDLE_PREFIX.equalsIgnoreCase(protocol)) {
                result = Configuration.getInstance().getHandleServerUrl() + resourceLink;
            }
        } catch (MalformedURLException e) {
            LOG.debug("URL error", e);
            //ignore exception, just show the original link, perhaps the user can do something with, we cannot do anything else.
        }
        return result;
    }

    private ImageResource getImage(String mimeType) {
        ImageResource image = ICON_MAP.get(CommonUtils.normalizeMimeType(mimeType));
        if (image == null) {
            image = TEXT; //unknow defaults to TEXT
        }
        return image;
    }

}
