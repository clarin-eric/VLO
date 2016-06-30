package eu.clarin.cmdi.vlo;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * Definition of facet, resource type and URL constants.
 */
public class FacetConstants {

    public static final String FIELD_ACCESS_INFO = "accessInfo";
    public static final String FIELD_AVAILABILITY = "availability";
    public static final String FIELD_COLLECTION = "collection";
    public static final String FIELD_COMPLETE_METADATA = "metadataSource";
    public static final String FIELD_CONTINENT = "continent";
    public static final String FIELD_COUNTRY = "country";
    public static final String FIELD_DATA_PROVIDER = "dataProvider";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_FORMAT = "format";
    public static final String FIELD_GENRE = "genre";
    public static final String FIELD_ID = "id";
    public static final String FIELD_KEYWORDS = "keywords";
    public static final String FIELD_LANGUAGE_CODE = "languageCode";
    public static final String FIELD_LICENSE = "license";
    public static final String FIELD_MODALITY = "modality";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_NATIONAL_PROJECT = "nationalProject";
    public static final String FIELD_ORGANISATION = "organisation";
    public static final String FIELD_PROJECT_NAME = "projectName";
    public static final String FIELD_RESOURCE_CLASS = "resourceClass";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_TEMPORAL_COVERAGE = "temporalCoverage";

    /**
     * Solr pseudo-field that reveals the ranking score
     *
     * @see
     * https://wiki.apache.org/solr/SolrRelevancyFAQ#How_can_I_see_the_relevancy_scores_for_search_results
     */
    public static final String FIELD_SOLR_SCORE = "score";

    //The _ facets are not meant to be shown to users.
    public static final String FIELD_SELF_LINK = "_selfLink";
    public static final String FIELD_FILENAME = "_fileName";
    public static final String FIELD_RESOURCE = "_resourceRef";
    public static final String FIELD_RESOURCE_COUNT = "_resourceRefCount";
    public static final String FIELD_CLARIN_PROFILE = "_componentProfile";
    public static final String FIELD_SEARCH_SERVICE = "_contentSearchRef";
    public static final String FIELD_LAST_SEEN = "_lastSeen";
    public static final String FIELD_DAYS_SINCE_LAST_SEEN = "_daysSinceLastSeen";
    public static final String FIELD_HIERARCHY_WEIGHT = "_hierarchyWeight";
    public static final String FIELD_IS_PART_OF = "_isPartOf";
    public static final String FIELD_HAS_PART = "_hasPart";
    public static final String FIELD_HAS_PART_COUNT = "_hasPartCount";
    public static final String FIELD_HAS_PART_COUNT_WEIGHT = "_hasPartCountWeight";
    public static final String FIELD_LANGUAGE_NAME = "_languageName";

    /**
     * Facet constant associated with the landing page type.
     */
    public static final String FIELD_LANDINGPAGE = "_landingPageRef";
    /**
     * Facet constant associated with the search page type.
     */
    public static final String FIELD_SEARCHPAGE = "_searchPageRef";

    /**
     * Fields for which a selection (by the user) should be allowed
     */
    public static final Set<String> AVAILABLE_FACETS = ImmutableSet.of(
            FIELD_ACCESS_INFO,
            FIELD_AVAILABILITY,
            FIELD_COLLECTION,
            FIELD_COMPLETE_METADATA,
            FIELD_CONTINENT,
            FIELD_COUNTRY,
            FIELD_DATA_PROVIDER,
            FIELD_DESCRIPTION,
            FIELD_FORMAT,
            FIELD_GENRE,
            FIELD_HAS_PART_COUNT,
            FIELD_ID,
            FIELD_KEYWORDS,
            FIELD_LANGUAGE_CODE,
            FIELD_LICENSE,
            FIELD_MODALITY,
            FIELD_NAME,
            FIELD_NATIONAL_PROJECT,
            FIELD_ORGANISATION,
            FIELD_PROJECT_NAME,
            FIELD_RESOURCE_CLASS,
            FIELD_SUBJECT,
            FIELD_TEMPORAL_COVERAGE,
            FIELD_SEARCH_SERVICE
    );

    //Deprecated fields
    public static final String DEPRECATED_FIELD_LANGUAGE = "language";

    //Normalized mimeTypes
    public static final String RESOURCE_TYPE_AUDIO = "audio";
    public static final String RESOURCE_TYPE_VIDEO = "video";
    public static final String RESOURCE_TYPE_TEXT = "text";
    public static final String RESOURCE_TYPE_IMAGE = "image";
    public static final String RESOURCE_TYPE_ANNOTATION = "annotation";

    /**
     * Handle proxy base url (to replace part that matches
     * {@link #HANDLE_PREFIX})
     */
    public static final String HANDLE_PROXY = "http://hdl.handle.net/";
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

}
