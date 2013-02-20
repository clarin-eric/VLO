package eu.clarin.cmdi.vlo.pages;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.CommonUtils;
import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;

import javax.net.ssl.HttpsURLConnection;

public class ResourceLinkPanel extends Panel {

    private final static Logger LOG = LoggerFactory.getLogger(ResourceLinkPanel.class);

    private static final long serialVersionUID = 1L;

    private final static String URN_NBN_RESOLVER_URL = "http://www.nbn-resolving.org/redirect/";

    private final static ImageResource ANNOTATION = new ImageResource(new ContextRelativeResource("Images/text-x-log.png"),
            "Annotation file");
    private final static ImageResource AUDIO = new ImageResource(new ContextRelativeResource("Images/audio-x-generic.png"), "Audio file");
    private final static ImageResource IMAGE = new ImageResource(new ContextRelativeResource("Images/image-x-generic.png"), "Image file");
    private final static ImageResource TEXT = new ImageResource(new ContextRelativeResource("Images/text-x-generic.png"), "Text file");
    private final static ImageResource VIDEO = new ImageResource(new ContextRelativeResource("Images/video-x-generic.png"), "Video file");

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
        String href = getHref(resourceLink);
        String name = getNameFromLink(resourceLink);
        ExternalLink link = new ExternalLink("resourceLink", href);
        link.add(resourceImg);
        link.add(new Label("resourceLabel", name));
        add(link);
    }

    /**
     * Modifies resourceLink if necessary (adds support for different URN resolvers)
     * @param resourceLink
     * @return Modified resourceLink, if no modifications are necessary original parameter resourceLink is returned
     */
    private String getHref(String resourceLink) {
        String result = resourceLink;
        if (resourceLink != null) {
            if (resourceLink.startsWith(FacetConstants.HANDLE_PREFIX)) {
                String handle = resourceLink.substring(FacetConstants.HANDLE_PREFIX.length());
                result = VloConfig.get().getHandleServerUrl() + handle;
            } else if(resourceLink.startsWith(FacetConstants.URN_NBN_PREFIX)) {
                result = URN_NBN_RESOLVER_URL+resourceLink;
            }
        }
        return result;
    }

    protected String getNameFromLink(String resourceLink) {
        String result = resourceLink;
        // We ALWAYS backoff to the resourceLink as default thingy.



     // HandleResolver does not work at the moment on the clarin server see http://trac.clarin.eu/ticket/136, Disabled it for the release.
//      if (resourceLink != null) {
//          if (resourceLink.startsWith(FacetConstants.HANDLE_PREFIX)) {
//              try {
//                  String handle = resourceLink.substring(FacetConstants.HANDLE_PREFIX.length());
//                  HandleResolver handleResolver = new HandleResolver();
//                  handleResolver.setTcpTimeout(5000);//5 secs, default is one minute
//                  HandleValue values[] = handleResolver.resolveHandle(handle, new String[] { "URL" }, null);
//
//                  for (HandleValue handleValue : values) {
//                      String url = handleValue.getDataAsString();
//                      int index = url.lastIndexOf('/');
//                      if (index != -1) {
//                          String name = url.substring(index + 1).trim();
//                          if (name.length() > 1) {
//                              result = name + " (" + resourceLink + ")";
//                          }
//                          break;
//                      }
//                  }
//              } catch (HandleException e) {
//                  LOG.warn("Error trying to get the name of the handle", e);
//              }
//          }
//      }

        /** NOTE: We are trying a different approach from the "official" one.
         * Will use the REST interface of hdl.handle.net.
         */
        if (resourceLink != null) {
            if (resourceLink.startsWith(FacetConstants.HANDLE_PREFIX)) {
                String handle = resourceLink.substring(FacetConstants.HANDLE_PREFIX.length());
                resourceLink = VloConfig.get().getHandleServerUrl() + handle;
                // Now points to something like http://hdl.handle.net/1839/00-0000-0000-0004-3357-F
                HttpURLConnection con = null;
                URL u;
                try{
                    u = new URL(resourceLink);
                    System.out.println(resourceLink);
                    URLConnection c = u.openConnection();
                    if(c instanceof HttpURLConnection){
                        con = (HttpURLConnection) c;
                    }
                    if(con != null){
                        if(con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM ||
                                con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP ||
                                con.getResponseCode() == HttpURLConnection.HTTP_SEE_OTHER){
                           result = con.getURL().toString();
/*                            for (Map.Entry<String, List<String>> header : con.getHeaderFields().entrySet()) {
                                if(header != null && header.getKey() != null){
                                    System.out.println("XXX:" + header.getKey());
                                    System.out.println(header.getValue().get(0));
                                    if(header.getKey().equals("Location")){
                                        result = header.getValue().get(0);
                                    }
                                }
                            }*/
                        }
                    }
                } catch (MalformedURLException e) {
                    LOG.warn("Error trying to get the name of the handle", e);
                } catch (IOException e) {
                    LOG.warn("Error trying to get the name of the handle", e);
                }
            }
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
