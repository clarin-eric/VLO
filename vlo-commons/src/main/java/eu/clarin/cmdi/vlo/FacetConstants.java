package eu.clarin.cmdi.vlo;

/**
 * Definition of facet, resource type and URL constants.
 */
public class FacetConstants {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";
    public static final String FIELD_DATA_PROVIDER = "dataProvider";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_COLLECTION = "collection";
    public static final String FIELD_COUNTRY = "country";
    public static final String FIELD_CONTINENT = "continent";
    public static final String FIELD_LANGUAGE = "language";
    public static final String FIELD_LANGUAGES = "languages";
    public static final String FIELD_YEAR = "year";
    public static final String FIELD_GENRE = "genre";
    public static final String FIELD_MODALITY = "modality";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_ORGANISATION = "organisation";
    public static final String FIELD_RESOURCE_CLASS = "resourceClass";
    public static final String FIELD_FORMAT = "format";
    public static final String FIELD_PROJECT_NAME = "projectName";
    public static final String FIELD_COMPLETE_METADATA = "metadataSource";
    public static final String FIELD_NATIONAL_PROJECT = "nationalProject";
    public static final String FIELD_KEYWORDS = "keywords";

    //The _ facets are not meant to be shown to users.
    public static final String FIELD_FILENAME = "_fileName";
    public static final String FIELD_RESOURCE = "_resourceRef";
    public static final String FIELD_CLARIN_PROFILE = "_componentProfile";
    public static final String FIELD_SEARCH_SERVICE = "_contentSearchRef";
    public static final String FIELD_LAST_SEEN = "_lastSeen";

    /**
     * Facet constant associated with the landing page type.
     */
    public static final String FIELD_LANDINGPAGE = "_landingPageRef";
    /**
     * Facet constant associated with the search page type.
     */
    public static final String FIELD_SEARCHPAGE = "_searchPageRef";

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
}
