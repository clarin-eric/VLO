package eu.clarin.cmdi.vlo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.FieldKey;

import java.util.ArrayList;
import java.util.Set;

/**
 * Definition of facet, resource type and URL constants.
 */
public class FacetConstants {
    public static final Set<String> getFieldList(FieldNameService fieldNameService, FieldKey... keys){
        ArrayList<String> list = new ArrayList<String>();
        for(FieldKey key: keys) {
            list.add(fieldNameService.getFieldName(key));
        }
        return ImmutableSet.copyOf(list);
    }
    
    public static final Set<String> getAvailableFacets(FieldNameService fieldNameService){
        return getFieldList(
            fieldNameService,
            FieldKey.ACCESS_INFO,
            FieldKey.AVAILABILITY,
            FieldKey.COLLECTION,
            FieldKey.COMPLETE_METADATA,
            FieldKey.CONTINENT,
            FieldKey.COUNTRY,
            FieldKey.DATA_PROVIDER,
            FieldKey.DESCRIPTION,
            FieldKey.FORMAT,
            FieldKey.GENRE,
            FieldKey.HAS_PART_COUNT,
            FieldKey.ID,
            FieldKey.KEYWORDS,
            FieldKey.LANGUAGE_CODE,
            FieldKey.LICENSE,
            FieldKey.LICENSE_TYPE,
            FieldKey.MODALITY,
            FieldKey.NAME,
            FieldKey.NATIONAL_PROJECT,
            FieldKey.ORGANISATION,
            FieldKey.PROJECT_NAME,
            FieldKey.RESOURCE_CLASS,
            FieldKey.SUBJECT,
            FieldKey.TEMPORAL_COVERAGE,
            FieldKey.SEARCH_SERVICE
        );
                
    }

    //Deprecated fields
//    public static final String DEPRECATED_FIELD_LANGUAGE = "language";

    //Normalized mimeTypes
    public static final String RESOURCE_TYPE_AUDIO = "audio";
    public static final String RESOURCE_TYPE_VIDEO = "video";
    public static final String RESOURCE_TYPE_TEXT = "text";
    public static final String RESOURCE_TYPE_IMAGE = "image";
    public static final String RESOURCE_TYPE_ARCHIVE = "archive";
    public static final String RESOURCE_TYPE_ANNOTATION = "annotation";

    /**
     * Handle proxy base url (to replace part that matches
     * {@link #HANDLE_PREFIX})
     */
    public static final String HANDLE_PROXY = "http://hdl.handle.net/";
    public static final String HANDLE_PROXY_HTTPS = "https://hdl.handle.net/";
    public static final String HANDLE_MPI_PREFIX = "hdl:1839";
    public static final String HANDLE_PREFIX = "hdl:";
    public static final String TEST_HANDLE_MPI_PREFIX = "test-hdl:1839";
    public static final String FIELD_RESOURCE_SPLIT_CHAR = "|";
    public static final String URN_NBN_PREFIX = "urn:nbn";
    public static final String URN_NBN_RESOLVER_URL = "http://www.nbn-resolving.org/redirect/";

    /**
     * regular expression that matches the language prefix in description (group
     * 1 matches the ISO639-3 language code)
     */
    public static final String DESCRIPTION_LANGUAGE_PATTERN = "^\\{(name|code):.*\\}";

    /**
     * regular expression that matches the syntax of the 'languageCode' field
     * (with either a language code or a name as indicated by the prefix)
     */
    public static final String LANGUAGE_CODE_PATTERN = "(name|code):(.*)";

    /**
     * Name of the Solr request handler for fast queries (no sorting, boosting
     * or aliases)
     */
    public static final String SOLR_REQUEST_HANDLER_FAST = "fast";

    /**
     * PUB level for the 'availability' facet
     *
     * @see #FIELD_AVAILABILITY
     */
    public static final String AVAILABILITY_LEVEL_PUB = "PUB";
    /**
     * ACA level for the 'availability' facet
     *
     * @see #FIELD_AVAILABILITY
     */
    public static final String AVAILABILITY_LEVEL_ACA = "ACA";
    /**
     * RES level for the 'availability' facet
     *
     * @see #FIELD_AVAILABILITY
     */
    public static final String AVAILABILITY_LEVEL_RES = "RES";

    public static final ImmutableList<String> LICENSE_TYPE_VALUES = ImmutableList.of(
            FacetConstants.AVAILABILITY_LEVEL_PUB,
            FacetConstants.AVAILABILITY_LEVEL_ACA,
            FacetConstants.AVAILABILITY_LEVEL_RES
    );

}
