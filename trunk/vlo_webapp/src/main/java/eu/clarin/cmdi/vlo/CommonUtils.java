package eu.clarin.cmdi.vlo;

import java.util.HashSet;
import java.util.Set;

public final class CommonUtils {

    private final static Set<String> ANNOTATION_MIMETYPES = new HashSet<String>();
    static {
        ANNOTATION_MIMETYPES.add("text/x-eaf+xml");
        ANNOTATION_MIMETYPES.add("text/x-shoebox-text");
        ANNOTATION_MIMETYPES.add("text/x-toolbox-text");
        ANNOTATION_MIMETYPES.add("text/x-chat");
    }
    private final static Set<String> TEXT_MIMETYPES = new HashSet<String>();
    static {
        TEXT_MIMETYPES.add("application/pdf");
    }

    private CommonUtils() {
    }

    public static String normalizeMimeType(String mimeType) {
        String type = mimeType;
        if (type != null) {
            type = type.toLowerCase();
        }
        String result = type;
        if (ANNOTATION_MIMETYPES.contains(type)) {
            result = FacetConstants.RESOURCE_TYPE_ANNOTATION;
        } else if (type.startsWith("audio")) {
            result = FacetConstants.RESOURCE_TYPE_AUDIO;
        } else if (type.startsWith("video")) {
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

}
