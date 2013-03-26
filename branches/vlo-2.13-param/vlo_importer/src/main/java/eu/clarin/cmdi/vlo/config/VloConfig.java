package eu.clarin.cmdi.vlo.config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;

/**
 * VLO configuration<br><br>
 * 
 * The annotated members in this class are the parameters by means of which
 * you can configure a VLO application.
 * 
 * A member is annotated by prepending {@literal @element} to it. When the 
 * VloConfig class is reflected into the Simple framework, the framework will
 * assign the values it finds in the {@literal VloConfig.xml} file to the 
 * members in the VloConfig class.
 * 
 * Whenever you need to add a parameter the VLO configuration, add a member 
 * with the appropriate name and type, and prepend an at sign to the 
 * declaration, like this:
 * 
 * {@literal @element}<br><br> 
 * {@literal parameterMember}<br><br>
 * 
 * The XML should in this case contain a definition like
 * 
 * {@literal<parameterMember>} "the value of the
 * parameter"{@literal </parameterMember>}<br><br>
 *
 * If you want to add a type of member that is not included in VloConfig class 
 * yet, or if you are looking for more information on the framework, please 
 * refer to VLO documentation.<br><br> 
 * 
 * In the VloConfig class, the parameters are stored statically. This means that
 * after 
 * 
 * {@literal VloConfig.readPackagedConfig();}<br><br>
 * 
 * has been issued from a certain context, you can reference a parameter by
 * 
 * {@literal WebAppConfig.getSomeParameter();}<br><br>
 *
 * in any scope, also a scope different from the one in which the read message
 * was send. 
 *
 * Through the get and set methods, the application is indifferent to the origin
 * of a parameter: you can get and set the value of a parameter without having
 * to worry about how the parameter was defined originally. 
 *
 * Also, the get and set methods allow for a modification of the original value
 * of the parameter. For example, if the format of a parameter changes, this 
 * change can be handled in the get and set methods, instead of having to 
 * modify every reference to the parameter in the application. 
 *
 * Note on the explanation of the meaning of the parameters. Because the
 * meaning of a parameter is not local to this class, or even not local to the
 * configuration package, they are described in the general VLO
 * documentation.<br><br>
 *
 * @author keeloo
 */
@Root
public class VloConfig extends ConfigFromFile {

    /** 
     * Create a reference through which a VloConfig class object can send
     * messages to the logging framework that has been associated with the 
     * application.
     */
    private final static org.slf4j.Logger LOG =
            LoggerFactory.getLogger(VloConfig.class);

    /**
     * Because the VloConfig class uses the Simple framework via the
     * ConfigFilePersister class, implement the logging interface in 
     * that class.
     */
    private class VloConfigLogger implements ConfigFilePersister.Logger {

        @Override
        public void log(Object data) {
            
            /**
             * Send a message to the logging framework by using the 
             * LOG reference.
             */
            LOG.error(data.toString());
        }
    }
  
    // create a reference to the ConfigFilePersister's logging interface
    private static VloConfigLogger logger;

    /**
     * Constructor method
     */
    public VloConfig() {
        // create the ConfigFilePersister's logging interface
        logger = new VloConfigLogger();

        /**
         * Initialize the ConfigFilePersister's reference to the interface 
         */
        ConfigFilePersister.setLogger(VloConfig.logger);
    }
    
    /**
     * Read the configuration from the packaged XML configuration file.  
     * 
     * @param fileName the name of the file to read the configuration from 
     */
    public static void readPackagedConfig() {
        
        // set the name of the packaged configuration file
        String fileName = "/VloConfig.xml";
        
        VloConfig.readConfig (fileName);
    }
    
    // VLO application configuration
    static VloConfig config = null;

    /**
     * Read the configuration from a file. 
     *
     * Please invoke this method instead of readPackagedConfig if you want to 
     * read the configuration from a file external to the VloConfig package.
     * 
     * @param fileName the name of the file to read the configuration from
     */
    public static void readConfig(String fileName) {

        if (config == null) {
            // the configuration is not there yet; create it now
            config = new VloConfig();
        }

        // get the XML file configuration from the file
        read(fileName, config);
    }

    /**
     * VLO application parameter members<br><br>
     *
     * Initialize the annotated members in a proper way. This will allow them to
     * be linearized to corresponding elements in an XML file.
     * 
     * Please refer to the general VLO documentation for a description of the
     * member parameters.
     */
    
    @Element // directive for Simple
    private static boolean deleteAllFirst = false;
    
    @Element 
    private static int maxOnHeap = 1000;
    
    @Element
    private static boolean printMapping = false;
    
    @ElementList // directive for Simple
    private static List<DataRoot> dataRoots;
    
    @Element
    private static String vloHomeLink = "";
    
    @Element
    private static String solrUrl = "";
    
    @Element
    private static String profileSchemaUrl = "";

    @Element
    private static String componentRegistryRESTURL = "";
    
    @Element
    private static String handleServerUrl = "";
    
    @Element
    private static String imdiBrowserUrl = "";
    
    /**
     * Note: the national project mapping itself is not part of the web
     * application configuration.
     */
    @Element
    private static String nationalProjectMapping = "";
    
    /**
     * An array of facetFields<br><br>
     *
     * In case of an array of elements, the number of elements in the array
     * needs to be communicated to the Simple framework. The following would be
     * a correct description of an array of three facet fields<br><br>
     *
     * {@literal <facetFields length="3">}<br> 
     * {@literal    <facetField>}<br>
     * {@literal       fieldOne}<br> 
     * {@literal    </facetField>}<br>
     * {@literal    <facetField>}<br> 
     * {@literal       fieldTwo}<br>
     * {@literal    </facetField>}<br> 
     * {@literal    <facetField>}<br>
     * {@literal       fieldThree}<br> 
     * {@literal    </facetField>}<br>
     * {@literal </facetFields>}<br><br>
     *
     * To let Simple now it has to interpret the facetFields element as an
     * array, use the ElementArray directive. Use the directive to let Simple
     * know that the elements inside 'facetFields' are named 'facetField'.
     */
    @ElementArray(entry = "facetField")
    private static String[] facetFields = {"", "", ""};
    
    @Element
    private static String countryComponentUrl = "";
    
    @Element
    private static String language2LetterCodeComponentUrl = "";
    
    @Element
    private static String language3LetterCodeComponentUrl = "";
    
    @Element
    private static String silToISO639CodesUrl = "";
    
    @Element
    private static String FederatedContentSearchUrl = " ";

    /**
     * Get and set methods for web application parameter members<br><br>
     *
     * By using a get or set method, you can apply an operation to a parameter
     * here without the need to make changes in different parts of the
     * application.
     */
    
    /**
     * Get the value of the maxOnHeap parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static int getMaxOnHeap (){
        return maxOnHeap;
    }
    
    
    /**
     * Set the value of the maxOnHeap parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static void setMaxOnHeap (int param){
        maxOnHeap = param;
    }
    
    /**
     * Get the list of dataRoots parameters<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static List<DataRoot> getDataRoots() {
        return dataRoots;
    }
    
    /**
     * Set the value of a list of dataRoots parameters<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param dataRoots the value
     */
    public static void setDataRoots(List<DataRoot> param) {
        dataRoots = param;
    }

    /**
     * Get the value of the deleteAllFirst parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static boolean deleteAllFirst() {
        return deleteAllFirst;
    }

    /**
     * Set the value of the deleteAllFirst parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static void setDeleteAllFirst(boolean param) {
        deleteAllFirst = param;
    }

    /**
     * Get the value of the printMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static boolean printMapping() {
        return printMapping;
    }
    
    /**
     * Set the value of the printMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param printMapping the value
     */
    public static void setPrintMapping(boolean param) {
        printMapping = param;
    }

    /**
     * Get the value of the VloHomeLink parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getVloHomeLink() {
        return vloHomeLink;
    }

    /**
     * Set the value of the VloHomeLink parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param vloHomeLink the value
     */
    public static void setVloHomeLink(String param) {
        vloHomeLink = param;
    }

    /**
     * Get the value of the SolrUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    static public String getSolrUrl() {
        return solrUrl;
    }

    /**
     * Set the value of the SolrUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param url the value
     */
    public static void setSolrUrl(String param) {
        solrUrl = param;
    }

    /**
     * Get the value of the ProfileSchemaUrl by profileId parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @return the value
     */
    public static String getComponentRegistryProfileSchema(String id) {
        return profileSchemaUrl.replace("{PROFILE_ID}", id);
    }

    /**
     * Set the value of the ProfileSchemaUrl parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @param profileId the value
     */
    public static void setProfileSchemaUrl(String param) {
        profileSchemaUrl = param;
    }

    /**
     * Get the value of the ComponentRegisteryRESTURL parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getComponentRegistryRESTURL() {
        return componentRegistryRESTURL;
    }

    /**
     * Set the value of the ComponentRegisteryRESTURL parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param componentRegistryRESTURL the value
     */
    public static void setComponentRegistryRESTURL(String param) {
        componentRegistryRESTURL = param;
    }

    /**
     * Get the value of the HandleServerUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getHandleServerUrl() {
        return handleServerUrl;
    }

    /**
     * Set the value of the HandleServerUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param handleServerUrl the value
     */
    public static void setHandleServerUrl(String param) {
        handleServerUrl = param;
    }

    /**
     * Set the value of the IMDIBrowserUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param imdiBrowserUrl the value
     */
    public static void setIMDIBrowserUrl(String param) {
        imdiBrowserUrl = param;
    }

    /**
     * Get the value of the ProfileSchemaUrl parameter combined with a handle<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     *
     * @return the value 
     */
    public static String getIMDIBrowserUrl(String handle) {
        String result;
        try {
            result = imdiBrowserUrl + URLEncoder.encode(handle, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = imdiBrowserUrl + handle;
        }
        return result;
    }

    /**
     * Get the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static void setFederatedContentSearchUrl(String param) {
        FederatedContentSearchUrl = param;
    }

    /**
     * Set the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param FederatedContentSearchUrl the value
     */
    public static String getFederatedContentSearchUrl() {
        return FederatedContentSearchUrl;
    }

    /**
     * Set the value of the NationalProjectMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param nationalProjectMapping the value
     */
    public static void setNationalProjectMapping(String param) {
        nationalProjectMapping = param;
    }
   

    /**
     * Get the value of the NationalProjectMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getNationalProjectMapping() {
        return nationalProjectMapping;
    }

    /**
     * Get the value of the FacetFields parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String[] getFacetFields() {
        return facetFields;
    }

    /**
     * Set the value of the FacetFields parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param facetFields the value, a list of facet fields
     */
    public static void setFacetFields(String[] param) {
        facetFields = param;
    }

    /**
     * Get the value of the CountryComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getCountryComponentUrl() {
        return countryComponentUrl;
    }

    /**
     * Set the value of the CountryComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param countryComponentUrl the value
     */
    public static void setCountryComponentUrl(String param) {
        countryComponentUrl = param;
    }

    /**
     * Get the value of the Language2LetterCodeComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getLanguage2LetterCodeComponentUrl() {
        return language2LetterCodeComponentUrl;
    }

    /**
     * Set the value of the Language2LetterCodeComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param language2LetterCodeComponentUrl the value
     */
    public static void setLanguage2LetterCodeComponentUrl(String param) {
        language2LetterCodeComponentUrl = param;
    }

    /**
     * Get the value of the Language3LetterCodeComponentUrl parameter<br><br> 
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getLanguage3LetterCodeComponentUrl() {
        return language3LetterCodeComponentUrl;
    }

    /**
     * Set the value of the Language3LetterCodeComponentUrl parameter<br><br> 
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param language3LetterCodeComponentUrl the value
     */
    public static void setLanguage3LetterCodeComponentUrl(String param) {
        language3LetterCodeComponentUrl = param;
    }

    /**
     * Get the value of the SilToISO639CodesUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getSilToISO639CodesUrl() {
        return silToISO639CodesUrl;
    }

    /**
     * Set the value of the SilToISO639CodesUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param silToISO639CodesUrl the value
     */
    public static void setSilToISO639CodesUrl(String param) {
        silToISO639CodesUrl = param;
    }
}