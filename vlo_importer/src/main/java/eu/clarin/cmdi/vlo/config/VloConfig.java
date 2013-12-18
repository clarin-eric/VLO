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
        
        // set lists to null because otherwise Simple will not overwrite it
        dataRoots = null;

        // get the XML file configuration from the file
        read(fileName, config);
    }

    /**
     * VLO application parameter members<br><br>
     *
     * Initialise the annotated members in a proper way. This will allow them to
     * be linearised to corresponding elements in an XML file.
     * 
     * Please refer to the general VLO documentation for a description of the
     * member parameters.
     */
    
    // data base related parameters
    
    @Element
    private static String solrUrl = "";
   
    @Element // directive for Simple
    private static boolean deleteAllFirst = false;
    
    @Element 
    private static int maxDocsInList = 0;
    
    @Element 
    private static int minDocsInSolrQueue = 0;
    
    @Element(required = false)
    private static int solrTimeOut = 0;
    
    // meta data input
    
    @ElementList // directive for Simple
    private static List<DataRoot> dataRoots;
    
    @Element 
    private static int maxFileSize = 0;
    
    @Element
    private static int maxDaysInSolr = 0;
    
    // mapping
    
    @Element(required = false)
    private static String facetConceptsFile = "";
    
    @ElementArray(entry = "languageFilter")
    private static String[] languageFilters = {"", "", ""};
    
    @Element
    private static boolean printMapping = false;
    
    @Element(required = false)
    private static String nationalProjectMapping = "";
    
    @Element
    private static String countryComponentUrl = "";
    
    @Element
    private static String language2LetterCodeComponentUrl = "";
    
    @Element
    private static String language3LetterCodeComponentUrl = "";
    
    @Element
    private static String silToISO639CodesUrl = "";
    
    // services
    
    @Element
    private static String FederatedContentSearchUrl = "";
    
    @Element
    private static boolean useHandleResolver = false;
    
    @Element
    private static String profileSchemaUrl = "";

    @Element
    private static String componentRegistryRESTURL = "";
    
    @Element
    private static String handleServerUrl = "";
    
    @Element
    private static String imdiBrowserUrl = "";
    
    @Element
    private static String languageLinkPrefix = "";
    
    // web application user interface 
    
    @Element
    private static int facetOverviewLength = 0;
    
    @Element
    private static String homeUrl = "";
    
    @Element
    private static String helpUrl = "";
    
    @Element
    private static String feedbackFromUrl = "";
    
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
    
    // test related parameters
    
    @Element(required = false)
    private static String reverseProxyPrefix = "";
    
    @Element(required = false)
    private static String cqlEndpointFilter = "";

    @Element(required = false)
    private static String cqlEndpointAlternative= "";
    

    /**
     * Get and set methods for web application parameter members<br><br>
     *
     * By using a get or set method, you can apply an operation to a parameter
     * here without the need to make changes in different parts of the
     * application.
     */
    
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
     * @param param the value
     */
    public static void setDeleteAllFirst(boolean param) {
        deleteAllFirst = param;
    }
    
    /**
     * Get the value of the maxDocsInList parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static int getMaxDocsInList (){
        return maxDocsInList;
    }
    
    /**
     * Set the value of the maxDocsInList parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setMaxDocsInList (int param){
        maxDocsInList = param;
    }
    
    /**
     * Get the value of the minDocsInSolrQueue parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static int getMinDocsInSolrQueue (){
        return minDocsInSolrQueue;
    }
    
    /**
     * Set the value of the minDocsInSolrQueue parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setMinDocsInSolrQueue (int param){
        minDocsInSolrQueue = param;
    }
    
    /**
     * Get the value of the solrTimeOut parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     * 
     * @return the value
     */
    public static int getSolrTimeOut (){
        return solrTimeOut;
    }
    
    /**
     * Set the value of the solrTimeOut parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setSolrTimeOut (int param){
        solrTimeOut = param;
    }
    
    /**
     * Get the value of the maxFileSize parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static int getMaxFileSize (){
        return maxFileSize;
    }
    
    /**
     * Set the value of the maxFileSize parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setMaxFileSize (int param){
        maxFileSize = param;
    }
    
    /**
     * Get the value of the maxDaysInSolr parameter<br><br>
     *
     * If the parameter is larger than 0, it denotes the maximal number of days
     * that a document can remain in the database.
     *
     * @return the value
     */
    public static int getMaxDaysInSolr (){
        return maxDaysInSolr;
    }
    
    /**
     * Set the value of the maxDaysInSolr parameter<br><br>
     *
     * If the parameter is larger than 0, it denotes the maximal number of days
     * that a document can remain in the database.
     *
     * @param param the value
     */
    public static void setMaxDaysInSolr (int param){
    	maxDaysInSolr = param;
    }
    
    /**
     * Get the value of the useHandleResolver parameter<br><br>
     *
     * The parameter can be used to reject meta data files that exceed some
     * maximum size.
     *
     * @return the value
     */
    public static boolean getUseHandleResolver (){
        return useHandleResolver;
    }
    
    /**
     * Set the value of the useHandleResolver parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setUseHandleResolver (boolean param){
        useHandleResolver = param;
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
     * @param param the value
     */
    public static void setDataRoots(List<DataRoot> param) {
        dataRoots = param;
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
     * @param param the value
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
    public static String getHomeUrl() {
        return homeUrl;
    }

    /**
     * Set the value of the VloHomeLink parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setHomeUrl(String param) {
        homeUrl = param;
    }

    /**
     * Get the value of the helpUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getHelpUrl() {
        return helpUrl;
    }

    /**
     * Set the value of the helpUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setHelpUrl(String param) {
        helpUrl = param;
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
     * @param param the value
     */
    public static void setSolrUrl(String param) {
        solrUrl = param;
    }
    
    /**
     * Get the value of the facetConceptsFile parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    static public String getFacetConceptsFile() {
        return facetConceptsFile;
    }

    /**
     * Set the value of the facetConceptsFile parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setFacetConceptsFile(String param) {
        facetConceptsFile = param;
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
     * @param param the value
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
     * @param param the value
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
     * @param param the value
     */
    public static void setHandleServerUrl(String param) {
        handleServerUrl = param;
    }

    /**
     * Get the value of the ProfileSchemaUrl parameter combined with a handle<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     *
     * @param handle the handle the URL is based on
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
     * Set the value of the IMDIBrowserUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setIMDIBrowserUrl(String param) {
        imdiBrowserUrl = param;
    }

    /**
     * Get the value of the languageLinkPrefix parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getLanguageLinkPrefix() {
        return languageLinkPrefix;
    }
    
    /**
     * Set the value of the languageLinkPrefix parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setLanguageLinkPrefix(String param) {
        languageLinkPrefix = param;
    }
    
    /**
     * Get the value of the feedbackFromUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getFeedbackFromUrl() {
        return feedbackFromUrl;
    }
    
    /**
     * Set the value of the feedbackFromUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setFeedbackFromUrl(String param) {
        feedbackFromUrl = param;
    }
    
    /**
     * Get the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getFederatedContentSearchUrl() {
        return FederatedContentSearchUrl;
    }

    /**
     * Set the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setFederatedContentSearchUrl(String param) {
        FederatedContentSearchUrl = param;
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
     * @param param the value, a list of facet fields
     */
    public static void setFacetFields(String[] param) {
        facetFields = param;
    }
    
    /**
     * Get the value of the languageFields parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String[] getLanguageFilters() {
        return languageFilters;
    }

    /**
     * Set the value of the languageFilters parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value, a list of language filters
     */
    public static void setLanguageFilters(String[] param) {
        languageFilters = param;
    }
    
    /**
     * Get the value of the getFacetOverviewLength parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static int getFacetOverviewLength() {
        return facetOverviewLength;
    }

    /**
     * Set the value of the setFacetOverviewLength parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public void setFacetOverviewLength(Integer param) {
        facetOverviewLength = param;
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
     * @param param the value
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
     * @param param the value
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
     * @param param the value
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
     * @param param the value
     */
    public static void setSilToISO639CodesUrl(String param) {
        silToISO639CodesUrl = param;
    }
    
    /**
     * Get the value of the reverseProxyPath parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getReverseProxyPrefix() {
        return reverseProxyPrefix;
    }

    /**
     * Set the value of the reverseProxyPrefix parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setReverseProxyPrefix(String param) {
        reverseProxyPrefix = param;
    }
    
    /**
     * Get the value of the cqlEndpointFilter parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getCqlEndpointFilter() {
        return cqlEndpointFilter;
    }

    /**
     * Set the value of the cqlEndpointFilter parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setCqlEndpointFilter(String param) {
        cqlEndpointFilter = param;
    }
    
    /**
     * Get the value of the cqlEndpointAlternative parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public static String getCqlEndpointAlternative() {
        return cqlEndpointAlternative;
    }

    /**
     * Set the value of the cqlEndpointAlternative parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param param the value
     */
    public static void setCqlEndpointAlternative(String param) {
        cqlEndpointAlternative = param;
    }
}