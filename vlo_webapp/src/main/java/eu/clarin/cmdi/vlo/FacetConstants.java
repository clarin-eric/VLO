package eu.clarin.cmdi.vlo;

public class FacetConstants {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";
    public static final String FIELD_DATA_PROVIDER = "dataProvider";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_COLLECTION = "collection";
    public static final String FIELD_COUNTRY = "country";
    public static final String FIELD_CONTINENT = "continent";
    public static final String FIELD_LANGUAGE = "language";
    public static final String FIELD_LANGUAGE_LINK = "languageLink";
    public static final String FIELD_YEAR = "year";
    public static final String FIELD_GENRE = "genre";
    public static final String FIELD_SUBJECT = "subject";
    public static final String FIELD_ORGANISATION = "organisation";
    public static final String FIELD_RESOURCE_TYPE = "resourceType"; 
    public static final String FIELD_PROJECT_NAME = "projectName";
    public static final String FIELD_COMPLETE_METADATA = "completeMD";
    public static final String FIELD_NATIONAL_PROJECT = "nationalProject";

    //The _ facets are not meant to be shown to users.
    public static final String FIELD_FILENAME = "_fileName";
    public static final String FIELD_RESOURCE = "_resourceRef";

    // PREFIX URL for the language-link
    public static final String LANGUAGE_LINK_PREFIX = "http://www.clarin.eu/external/language.php?code=";

    //Normalized mimeTypes
    public static final String RESOURCE_TYPE_AUDIO = "audio";
    public static final String RESOURCE_TYPE_VIDEO = "video";
    public static final String RESOURCE_TYPE_TEXT = "text";
    public static final String RESOURCE_TYPE_IMAGE = "image";
    public static final String RESOURCE_TYPE_ANNOTATION = "annotation";
    
    public static final String HANDLE_MPI_PREFIX = "hdl:1839";
    public static final String HANDLE_PREFIX = "hdl:";
    public static final String TEST_HANDLE_MPI_PREFIX = "test-hdl:1839";
    public static final String FIELD_RESOURCE_SPLIT_CHAR = "|";

}
