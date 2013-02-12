
package eu.clarin.cmdi.vlo.config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;

/**
 * Web application configuration<br><br>
 * 
 * The parameter members which are part of the web application configuration
 * come in two categories. One type of parameters play a role only in the
 * application, others are determined by the context of the application.<br><br>
 *
 * The first type of parameter is defined in the web application parameter file.
 * These parameters are referred to as application parameters. The second type
 * of parameter is determined by the context of the application. These
 * parameters are called context parameters.<br><br>
 *
 * Because the application is indifferent to the origin of a parameter, the
 * WebAppConfig class is the place to switch from one type of parameter to the
 * other. In other words: you do not have to change the application if you
 * replace an application parameter by a context parameter. The change is
 * reflected here, and not in the application. In a sense, this relieves the
 * need to use get and read methods. Such methods still have an advantage
 * though, because you can change a parameter without changing the rest of the
 * application.<br><br>
 *
 * An application parameter is defined an XML file in the resources directory of
 * the package. For every application parameter, the WebApplication class
 * contains a member that is annotated according to the Simple framework
 * specification. So<br><br>
 *
 * {@literal <parameterMember>}"the value of the
 * parameter"{@literal </parameterMember>}<br><br>
 *
 * in the XML file is accompanied by<br><br>
 *
 * {@literal
 *
 * @element}<br> {@literal parameterMember}<br><br>
 *
 * in the WebAppConfig class. When the application invokes Simple by<br><br>
 *
 * WebAppConfig.get();<br><br>
 *
 * the parameter itself is accessed by<br><br>
 *
 * WebAppConfig.get().getParameterMember();<br><br>
 * 
 * If you want to add a type of member that is not included in the class yet,
 * refer to the Simple framework's specification.<br><br>
 *
 * A context parameter also resides in an XML file. For more information on the
 * location and the format of this file, please refer to the Apache Tomcat
 * configuration reference.<br><br>
 *
 * Note on the explanation of the meaning of the parameters. Because the meaning
 * of a parameter is not local to this class, or even not local to the
 * configuration package, they are described in the general VLO
 * documentation.<br><br>
 *
 * @author keeloo
 */
@Root
public class WebAppConfig extends ConfigFileParam {

    // create a reference to the application logging
    private final static org.slf4j.Logger LOG =
            LoggerFactory.getLogger(WebAppConfig.class);
    
    // connect to the logging framework
    private class WebAppConfigLogger implements ConfigFilePersister.Logger {

        @Override
        public void log(Object data) {

            LOG.error(data.toString());
        }
    }
    
    private static WebAppConfigLogger logger;

    /**
     * Constructor method
     */
    public WebAppConfig() {
        // let the superclass know about the logger defined here
        // ConfigFileParam.logger = webAppConfigLogger;
        
        logger = new WebAppConfigLogger();
        
        ConfigFilePersister.setLogger(WebAppConfig.logger);
    }

    /**
     * Application parameter XML file.
     */
    public static final String CONFIG_FILE = WebAppConfig.class.getResource("/WebAppConfig.xml").getFile();

    /**
     * Application parameter XML file. Represent the filename by a method
     * primarily for making it available to the superclass. In other words: this
     * method overrides a method in the superclass.
     *
     * @return the name of the application parameter XML file
     */
    @Override
    public String getFileName() {
        /**
         * Check the name of the web application parameter file. May be turn it
         * into a {@literal maven} parameter.
         */
        return CONFIG_FILE;
    }
    
    /**
     * Make the configuration accessible<br><br>
     * 
     * Both the Simple framework and the methods in the web application need to
     * access the configuration. Access is granted by defining a member holding
     * the configuration, that is, by defining a member of type WebAppConfig.
     */
    private static WebAppConfig config = null;

    /**
     * Make the configuration statically accessible<br><br>
     *
     * Access for the Simple framework is achieved by passing the member to the
     * read method defined in the superclass.<br><br>
     * 
     * Access for the web application is achieved by invoking one of the get or
     * read methods. Because these methods return a non-static value, while 
     * WebAppConfig on the other hand denotes a static context,<br><br>
     *
     * WebAppConfig.getParameterMember()<br><br>
     *
     * for example, would not be valid. On encountering get() however, a new
     * context is opened, and from that, getParameterMember() can be
     * invoked:<br><br>
     *
     * WebAppConfig.get().getParameterMember()<br><br>
     *
     * @return the web application configuration in a new context
     */
    public static WebAppConfig get() {
        if (config == null) {
            // the configuration is not there yet; create it now
            config = new WebAppConfig();
        }

        // get the xml file configuration by invoking the superclass method 
        config = (WebAppConfig) read(config);

        /* Contrary to Simple, the web context parameters are not retrieved
         * by annotation. Get them by invoking a local method. 
         */
        // config = addFromContext(config);

        return config;
    }
    
    /**
     * Web application parameter members<br><br>
     * 
     * Initialize a member corresponding to application parameters with an empty
     * string at least, for this will allow them to be linearized to
     * corresponding elements in the XML file. Note that for the moment only
     * de-linearization is supported by the WebAppConfig class.
     *
     * Refer to the general VLO documentation for a description of the member
     * parameters.
     */

    @Element // annotation directive as defined by Simple
    private String vloHomeLink = "";

    // In the XML file, the value of the parameter is expanded by Maven.
    @Element
    private String profileSchemaUrl = "";

    @Element
    private String componentRegistryRESTURL = "";
    
    @Element
    private String handleServerUrl = "";

    // In the XML file, the value of the parameter is expanded by Maven.
    @Element
    private String solrUrl = "";
    
    @Element
    private String imdiBrowserUrl = "";
    
    /**
     * Note. The national project mapping itself is not part of the web
     * application configuration.
     */
    @Element
    private String nationalProjectMapping = "";
    
    /**
     * An array of facetFields<br><br>
     * 
     * In case of an array of elements, the number of elements in the array
     * needs to be communicated to Simple. The following would be a correct
     * description of an array of three facet fields<br><br>
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
     * To let Spring now it has to interpret the facetFields element as an
     * array, use the ElementArray directive. Use the directive to let Spring
     * know that the elements inside 'facetFields' are named 'facetField'. 
     */

    @ElementArray(entry="facetField")
    private String[] facetFields = {"", "", ""};
    
    @Element
    private String countryComponentUrl = "";
    
    @Element
    private String language2LetterCodeComponentUrl ="";
    
    @Element
    private String language3LetterCodeComponentUrl = "";
    
    @Element
    private String silToISO639CodesUrl = "";
    
    @Element
    private String FederatedContentSearchUrl = " ";
    
    /**
     * Get and read methods for web application parameter members<br><br>
     * 
     * By using a get or read method, you can apply an operation to a parameter
     * here, in the WebAppConfig class, without the need to make changes in
     * different parts of the application.
     */
    
    /**
     * Get the VloHomeLink parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String getVloHomeLink() {
        return vloHomeLink;
    }

    /**
     * Set the VloHomeLink parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param vloHomeLink the parameter
     */
    public void setVloHomeLink(String vloHomeLink) {
        this.vloHomeLink = vloHomeLink;
    }

    /**
     * Get the ProfileSchemaUrl by profileId parameter<br><br>
     * 
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @return the parameter
     */
    public String getComponentRegistryProfileSchema(String profileId) {
        return profileSchemaUrl.replace("${PROFILE_ID}", profileId);
    }

    /**
     * Set the ProfileSchemaUrl parameter<br><br>
     * 
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @param profileId the parameter
     */
    public void setProfileSchemaUrl(String profileSchemaUrl) {
        this.profileSchemaUrl = profileSchemaUrl;
    }

    /**
     * Get the ComponentRegisteryRESTURL parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String getComponentRegistryRESTURL() {
        return componentRegistryRESTURL;
    }

    /**
     * Set the ComponentRegisteryRESTURL parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     * 
     * @param componentRegistryRESTURL the parameter
     */
    public void setComponentRegistryRESTURL(String componentRegistryRESTURL) {
        this.componentRegistryRESTURL = componentRegistryRESTURL;
    }

    /**
     * Get the HandleServerUrl parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String getHandleServerUrl() {
        return handleServerUrl;
    }

    /**
     * Set the HandleServerUrl parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     * 
     * @param handleServerUrl the parameter
     */
    public void setHandleServerUrl(String handleServerUrl) {
        this.handleServerUrl = handleServerUrl;
    }

    /**
     * Get the SolrUrl parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }

    /**
     * Set the SolrUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param solrUrl the parameter
     */
    public String getSolrUrl() {
        return solrUrl;
    }

    /**
     * Set the IMDIBrowserUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param imdiBrowserUrl the parameter
     */
    public void setIMDIBrowserUrl(String imdiBrowserUrl) {
        this.imdiBrowserUrl = imdiBrowserUrl;
    }

    /**
     * Get ProfileSchemaUrl parameter combined with a handle<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     *
     * @param handle the handle to be combined with the parameter
     */
    public String getIMDIBrowserUrl(String handle) {
        String result;
        try {
            result = imdiBrowserUrl + URLEncoder.encode(handle, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = imdiBrowserUrl + handle;
        }
        return result;
    }
    

    /**
     * Get the FederatedContentSearchUrl parameter<br><br>
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public void setFederatedContentSearchUrl(String solrUrl) {
        this.FederatedContentSearchUrl = FederatedContentSearchUrl;
    }

    /**
     * Set the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param FederatedContentSearchUrl the parameter
     */
    public String getFederatedContentSearchUrl() {
        return FederatedContentSearchUrl;
    }

    /**
     * Set the NationalProjectMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param nationalProjectMapping the parameter
     */
    public void setNationalProjectMapping(String nationalProjectMapping) {
        this.nationalProjectMapping = nationalProjectMapping;
    }

    /**
     * Get the NationalProjectMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String getNationalProjectMapping() {
        return nationalProjectMapping;
    }

    /**
     * Get the FacetFields parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String[] getFacetFields() {
        return facetFields;
    }

    /**
     * Set the FacetFields parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param facetFields the parameter, a list of facet fields
     */
    public void setFacetFields(String[] facetFields) {
        this.facetFields = facetFields;
    }

    /**
     * Get the CountryComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String getCountryComponentUrl() {
        return countryComponentUrl;
    }

    /**
     * Set the CountryComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param countryComponentUrl the parameter
     */
    public void setCountryComponentUrl(String countryComponentUrl) {
        this.countryComponentUrl = countryComponentUrl;
    }

    /**
     * Get the Language2LetterCodeComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String getLanguage2LetterCodeComponentUrl() {
        return language2LetterCodeComponentUrl;
    }

    /**
     * Set the Language2LetterCodeComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param language2LetterCodeComponentUrl the parameter
     */
    public void setLanguage2LetterCodeComponentUrl(String language2LetterCodeComponentUrl) {
        this.language2LetterCodeComponentUrl = language2LetterCodeComponentUrl;
    }

    /**
     * Get the Language3LetterCodeComponentUrl parameter<br><br> For a
     * description of the parameter, refer to the general VLO documentation.
     *
     * @return the parameter
     */
    public String getLanguage3LetterCodeComponentUrl() {
        return language3LetterCodeComponentUrl;
    }

    /**
     * Set the Language3LetterCodeComponentUrl parameter<br><br> For a
     * description of the parameter, refer to the general VLO documentation.
     *
     * @param language3LetterCodeComponentUrl the parameter
     */
    public void setLanguage3LetterCodeComponentUrl(String language3LetterCodeComponentUrl) {
        this.language3LetterCodeComponentUrl = language3LetterCodeComponentUrl;
    }

    /**
     * Get the SilToISO639CodesUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the parameter
     */
    public String getSilToISO639CodesUrl() {
        return silToISO639CodesUrl;
    }

    /**
     * Set the SilToISO639CodesUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param silToISO639CodesUrl the parameter
     */
    public void setSilToISO639CodesUrl(String silToISO639CodesUrl) {
        this.silToISO639CodesUrl = silToISO639CodesUrl;
    }
    
    /**
     * Retrieve the context parameters<br><br>
     *
     * Note that in terms of the configuration, properties of the context are
     * treated as parameters also.
     *
     * @param config the static WebAppConfig member
     *
     * @return the static WebAppConfig member
     */
    static WebAppConfig addFromContext(WebAppConfig config) {
        
        // retrieve parameters from the context of the web application
        
        docBase = WebAppParam.getContextParam ("docBase");
        
        return config;
    }
    
    /**
     * Web application context members<br><br>
     *
     * The following defines the members corresponding to context.
     */
    
    /**
     * Context members
     */
    static String docBase = "";
    
    /**
     * Get and read methods for the context members
     */
    
    /**
     *
     * @return
     */
    public String getDocbaBase() {
        return docBase;
    }
    
    /**
     *
     * @return
     */
    public String setDocBase(String param) {
        // potentially, modify the parameter in the configuration
        docBase = param; 
        return docBase;
    }
}
