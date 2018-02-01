package eu.clarin.cmdi.vlo;

import com.google.common.collect.ImmutableSet;
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
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;

public final class CommonUtils {

    private final static Set<String> ANNOTATION_MIMETYPES = ImmutableSet.of(
            "text/x-eaf+xml",
            "text/x-shoebox-text",
            "text/x-toolbox-text",
            "text/x-chat",
            "text/x-chat",
            "application/mediatagger",
            "mt",
            "application/smil+xml");

    private final static Set<String> TEXT_MIMETYPES = ImmutableSet.of(
            "application/pdf",
            "txt");

    private final static Set<String> VIDEO_MIMETYPES = ImmutableSet.of(
            "application/mxf");

    private final static Set<String> AUDIO_MIMETYPES = ImmutableSet.of(
            "application/ogg",
            "wav");

    private final static Set<String> ARCHIVE_MIMETYPES = ImmutableSet.of(
            "application/tar", "application/tar+gzip",
            "application/zip", "application/zip-compressed",
            "application/gzip", "application/gzip-compressed",
            "application/x-bzip", "application/x-bzip2", "application/x-bz2",
            "application/x-compress", "application/x-compressed",
            "application/x-rar-compressed",
            "application/x-gtar",
            "application/x-gzip",
            "application/x-tar", "application/x-tar-gz",
            "application/x-zip", "application/x-zip-compressed",
            "application/x-7z-compressed", "application/x-7zip-compressed",
            "application/x-xz"
    );

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
        } else if (ARCHIVE_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_ARCHIVE;
        }
        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <code>&lt;item AppInfo="Tigrinya (ti)"&gt;ti&lt;/item&gt;</code> Will become key (after
     * removal of trailing 2 or 3 letter codes if value is unique), values: ti, Tigrinya
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
        final Map<String, Integer> tmpCount = new HashMap<>();
        final String replacementString = " \\([a-zA-Z]+\\)$";

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
            String appInfoText = appInfoAttribute.evalXPathToString();
            String itemContent = nav.toNormalizedString(nav.getText());
            result.put(itemContent.toUpperCase(), appInfoText);

            // count how often appInfoText String (without extension in parentheses) occurs
            String reducedAppInfoText = appInfoText.replaceAll(replacementString, "");
            if(!tmpCount.containsKey(reducedAppInfoText))
                tmpCount.put(reducedAppInfoText, 1);
            else
                tmpCount.put(reducedAppInfoText, tmpCount.get(reducedAppInfoText)+1);
        }

        // postprocessing: 2 or 3 letter codes will only be removed if name (e.g. language name) is unique
        for(String key : result.keySet()) {
            String value = result.get(key);
            String reducedValue = value.replaceAll(replacementString, "");
            if(tmpCount.get(reducedValue) == 1)
                result.put(key, reducedValue);
        }

        return result;
    }

    /**
     * Create a mapping out of simple CMDI components for instance: lists of
     * items: <code>&lt;item AppInfo="Tigrinya"&gt;ti&lt;/item&gt;</code> Will become key (after removal
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
