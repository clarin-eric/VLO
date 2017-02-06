package eu.clarin.cmdi.vlo;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;


public final class CommonUtils {

    private final static Set<String> ANNOTATION_MIMETYPES = new HashSet<>();

    static {
        ANNOTATION_MIMETYPES.add("text/x-eaf+xml");
        ANNOTATION_MIMETYPES.add("text/x-shoebox-text");
        ANNOTATION_MIMETYPES.add("text/x-toolbox-text");
        ANNOTATION_MIMETYPES.add("text/x-chat");
        ANNOTATION_MIMETYPES.add("text/x-chat");
        ANNOTATION_MIMETYPES.add("application/mediatagger");
        ANNOTATION_MIMETYPES.add("mt");
        ANNOTATION_MIMETYPES.add("application/smil+xml");
    }
    private final static Set<String> TEXT_MIMETYPES = new HashSet<>();

    static {
        TEXT_MIMETYPES.add("application/pdf");
        TEXT_MIMETYPES.add("txt");
    }
    private final static Set<String> VIDEO_MIMETYPES = new HashSet<>();

    static {
        VIDEO_MIMETYPES.add("application/mxf");
    }
    private final static Set<String> AUDIO_MIMETYPES = new HashSet<>();

    static {
        AUDIO_MIMETYPES.add("application/ogg");
        AUDIO_MIMETYPES.add("wav");
    }

    /**
     * Set system property {@code vlo.swallowLookupErrors} to 'true' to make
     * run/import possible without a network connection
     *
     * @return whether {@code vlo.swallowLookupErrors} equals 'true'
     */
    public static Boolean shouldSwallowLookupErrors() {
        final String propVal = System.getProperty("vlo.swallowLookupErrors", "false");
        return Boolean.valueOf(propVal);
    }

    private CommonUtils() {
    }

    public static String normalizeMimeType(String mimeType) {
        String type = mimeType;
        if (type != null) {
            type = type.toLowerCase();
        } else {
            type = "";
        }
        String result = "unknown type";
        if (ANNOTATION_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_ANNOTATION;
        } else if (type.startsWith("audio") || AUDIO_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_AUDIO;
        } else if (type.startsWith("video") || VIDEO_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_VIDEO;
        } else if (type.startsWith("image")) {
            result = FacetConstants.RESOURCE_TYPE_IMAGE;
        } else if (type.startsWith("audio")) {
            result = FacetConstants.RESOURCE_TYPE_AUDIO;
        } else if (type.startsWith("text") || TEXT_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_TEXT;
        }
        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <item AppInfo="Tigrinya (ti)">ti</item> Will become key (after
     * removal of trailing 2 or 3 letter codes), values: ti, Tigrinya
     *
     * @param urlToComponent
     * @return Map with item_value, AppInfo_value pairs
     * @throws MalformedURLException
     * @throws com.ximpleware.ParseException
     * @throws com.ximpleware.NavException
     * @throws com.ximpleware.XPathParseException
     * @throws com.ximpleware.XPathEvalException
     */
    public static Map<String, String> createCMDIComponentItemMap(String urlToComponent) throws IOException, ParseException, NavException,
            XPathParseException, XPathEvalException {
        final Map<String, String> result = new HashMap<>();

        URL url = new URL(urlToComponent);
        VTDGen vg = new VTDGen();

        vg.setDoc(IOUtils.toByteArray(url.openStream()));
        vg.parse(true);
        final VTDNav nav = vg.getNav();
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        ap.selectXPath("//item");

        AutoPilot appInfoAttribute = new AutoPilot(nav);
        appInfoAttribute.selectXPath("@AppInfo");

        while (ap.evalXPath() != -1) {
            String appInfoText = appInfoAttribute.evalXPathToString().replaceAll(" \\([a-zA-Z]+\\)$", "");
            String itemContent = nav.toNormalizedString(nav.getText());
            result.put(itemContent.toUpperCase(), appInfoText);
        }

        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <item AppInfo="Tigrinya">ti</item> Will become key (after removal
     * of trailing 2 or 3 letter codes), values: Tigrinya, ti
     *
     * @param urlToComponent
     * @return Map with item_value, AppInfo_value pairs
     * @throws java.io.IOException
     * @throws com.ximpleware.ParseException
     * @throws com.ximpleware.NavException
     * @throws com.ximpleware.XPathParseException
     * @throws com.ximpleware.XPathEvalException
     */
    public static Map<String, String> createReverseCMDIComponentItemMap(String urlToComponent) throws IOException, ParseException, NavException,
            XPathParseException, XPathEvalException {
        final Map<String, String> result = new HashMap<>();

        URL url = new URL(urlToComponent);
        VTDGen vg = new VTDGen();

        vg.setDoc(IOUtils.toByteArray(url.openStream()));
        vg.parse(true);
        final VTDNav nav = vg.getNav();
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        ap.selectXPath("//item");

        AutoPilot appInfoAttribute = new AutoPilot(nav);
        appInfoAttribute.selectXPath("@AppInfo");

        while (ap.evalXPath() != -1) {
            String appInfoText = appInfoAttribute.evalXPathToString().replaceAll(" \\([a-zA-Z]+\\)$", "");
            String itemContent = nav.toNormalizedString(nav.getText());
            result.put(appInfoText, itemContent);
        }

        return result;
    }

}
