package eu.clarin.cmdi.vlo.config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.servlet.ServletContext;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;

/**
 * Web application configuration<br><br>
 * 
 * Map the elements in the packaged {@literal WebAppConfig.xml} file to the
 * members in this class, the configuration of the VLO web application and
 * importer according to the Simple framework specification. So<br><br>
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
 * in the VloConfig class. If you want to add a type of member that is not
 * included in the class yet, refer to the Simple framework's
 * specification.<br><br> 
 * 
 * The parameters are stored statically. This means that a parameter can be
 * referenced from the application without first creating a configuration
 * object. So get() in
 *
 * WebAppConfig.get().getSomeParameter();<br><br>
 *
 * will return the static configuration, and getSomeParameter() will return a
 * specific parameter in this configuration. 
 * 
 * Through the get and set methods, the application is indifferent to the origin
 * of a parameter: you can get and set the value of a parameter without having
 * to worry about how the parameter was defined originally. By invoking the read
 * method, and by querying the context, the web application, on initialization,
 * determines which definition to use. 
 * 
 * Also, the get and set methods allow for a modification of the original value
 * of the parameter. For example, if the format of a parameter changes, this 
 * change can be handled in the get and set method once, instead of having to 
 * modify every reference to the parameter in the application. 
 *
 * Please note on the explanation of the meaning of the parameters. Because the
 * meaning of a parameter is not local to this class, or even not local to the
 * configuration package, they are described in the general VLO
 * documentation.<br><br>
 *
 * @author keeloo
 */
@Root
public class VloConfig extends ConfigFromFile {

    // create a reference to the application logging
    private final static org.slf4j.Logger LOG =
            LoggerFactory.getLogger(VloConfig.class);

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
    public VloConfig() {
        // let the superclass know about the logger defined here

        logger = new WebAppConfigLogger();

        ConfigFilePersister.setLogger(VloConfig.logger);
    }
    
    /**
     * Make the configuration statically accessible
     */
    private static VloConfig config = null;

    /**
     * Read the configuration from an XML file. 
     *
     * Please invoke this method from the web application or from the importer;
     * the readTestConfig method is intended for testing purposes.
     *
     * @param fileName
     *
     * @return the web application configuration 
     */
    public static VloConfig readConfig(String fileName) {
        if (config == null) {
            // the configuration is not there yet; create it now
            config = new VloConfig();
        }

        // get the XML file configuration from the file by invoking the

        config = (VloConfig) read(fileName, config);

        return config;
    }

    /**
     * Read the configuration from an XML file. 
     * 
     * Please invoke this method from the package tests. If the tests invoke a
     * method different from the one used by the web application and the
     * important, you can make some test specific changes to the parameters
     * here. 
     * 
     * @param fileName
     *
     * @return the web application configuration in a new static context
     */
    public static VloConfig readTestConfig(String fileName) {
        if (config == null) {
            // the configuration is not there yet; create it now
            config = new VloConfig();
        }

        // get the XML file configuration from the file by invoking the

        config = (VloConfig) read(fileName, config);

        return config;
    }
    
    /**
     * Return the configuration
     * 
     * @return 
     */
    public static VloConfig get (){
        return config;
    }

    /**
     * VLO application parameter members<br><br>
     *
     * Initialize a member corresponding to application parameters with an empty
     * string at least, for this will allow them to be linearized to
     * corresponding elements in the XML file.
     * 
     * Please refer to the general VLO documentation for a description of the
     * member parameters.
     */
    
    @Element // directive for Simple
    private boolean deleteAllFirst = false;
    
    @Element
    private boolean printMapping = false;
    
    @ElementList // directive for Simple
    private List<DataRoot> dataRoots;
    
    public List<DataRoot> getDataRoots() {
        return dataRoots;
    }
    
    @Element
    private String vloHomeLink = "";
    
    @Element
    private String solrUrl = "";
    
    @Element
    private String profileSchemaUrl = "";

    @Element
    private String componentRegistryRESTURL = "";
    
    @Element
    private String handleServerUrl = "";
    
    @Element
    private String imdiBrowserUrl = "";
    
    /**
     * Note: the national project mapping itself is not part of the web
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
     * To let Simple now it has to interpret the facetFields element as an
     * array, use the ElementArray directive. Use the directive to let Simple
     * know that the elements inside 'facetFields' are named 'facetField'.
     */
    @ElementArray(entry = "facetField")
    private String[] facetFields = {"", "", ""};
    
    @Element
    private String countryComponentUrl = "";
    
    @Element
    private String language2LetterCodeComponentUrl = "";
    
    @Element
    private String language3LetterCodeComponentUrl = "";
    
    @Element
    private String silToISO639CodesUrl = "";
    
    @Element
    private String FederatedContentSearchUrl = " ";

    /**
     * Get and set methods for web application parameter members<br><br>
     *
     * By using a get or set method, you can apply an operation to a parameter
     * here without the need to make changes in different parts of the
     * application.
     */
    
    /**
     * Set the value of the dataRoots parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param the value
     */
    public void setDataRoots(List<DataRoot> dataRoots) {
        this.dataRoots = dataRoots;
    }

    /**
     * Set the value deleteAllFirst parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public void setDeleteAllFirst(boolean deleteAllFirst) {
        this.deleteAllFirst = deleteAllFirst;
    }

    /**
     * Get the value of the deleteAllFirst parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public boolean isDeleteAllFirst() {
        return deleteAllFirst;
    }

    /**
     * Set the value of the printMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param the value
     */
    public void setPrintMapping(boolean printMapping) {
        this.printMapping = printMapping;
    }

    /**
     * Get the value of the printMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public boolean isPrintMapping() {
        return printMapping;
    }
    
    /**
     * Get the value of the VloHomeLink parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getVloHomeLink() {
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
    public void setVloHomeLink(String link) {
        this.vloHomeLink = link;
    }

    /**
     * Get the value of the SolrUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getSolrUrl() {
        return solrUrl;
    }

    /**
     * Set the value of the SolrUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param the parameter
     */
    public void setSolrUrl(String url) {
        this.solrUrl = url;
    }

    /**
     * Get the value of the ProfileSchemaUrl by profileId parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @return the value
     */
    public String getComponentRegistryProfileSchema(String id) {
        return profileSchemaUrl.replace("${PROFILE_ID}", id);
    }

    /**
     * Set the value of the ProfileSchemaUrl parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @param profileId the value
     */
    public void setProfileSchemaUrl(String url) {
        this.profileSchemaUrl = url;
    }

    /**
     * Get the value of the ComponentRegisteryRESTURL parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getComponentRegistryRESTURL() {
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
    public void setComponentRegistryRESTURL(String url) {
        this.componentRegistryRESTURL = url;
    }

    /**
     * Get the value of the HandleServerUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getHandleServerUrl() {
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
    public void setHandleServerUrl(String url) {
        this.handleServerUrl = url;
    }

    /**
     * Set the value of the IMDIBrowserUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param imdiBrowserUrl the value
     */
    public void setIMDIBrowserUrl(String url) {
        this.imdiBrowserUrl = url;
    }

    /**
     * Get the value of the ProfileSchemaUrl parameter combined with a handle<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     *
     * @param handle the value 
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
     * Get the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public void setFederatedContentSearchUrl(String url) {
        this.FederatedContentSearchUrl = url;
    }

    /**
     * Set the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param FederatedContentSearchUrl the value
     */
    public String getFederatedContentSearchUrl() {
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
    public void setNationalProjectMapping(String mapping) {
        this.nationalProjectMapping = mapping;
    }

    /**
     * Get the value of the NationalProjectMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getNationalProjectMapping() {
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
    public String[] getFacetFields() {
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
    public void setFacetFields(String[] fields) {
        this.facetFields = fields;
    }

    /**
     * Get the value of the CountryComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getCountryComponentUrl() {
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
    public void setCountryComponentUrl(String url) {
        this.countryComponentUrl = url;
    }

    /**
     * Get the value of the Language2LetterCodeComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getLanguage2LetterCodeComponentUrl() {
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
    public void setLanguage2LetterCodeComponentUrl(String url) {
        this.language2LetterCodeComponentUrl = url;
    }

    /**
     * Get the value of the Language3LetterCodeComponentUrl parameter<br><br> 
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getLanguage3LetterCodeComponentUrl() {
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
    public void setLanguage3LetterCodeComponentUrl(String url) {
        this.language3LetterCodeComponentUrl = url;
    }

    /**
     * Get the value of the SilToISO639CodesUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getSilToISO639CodesUrl() {
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
    public void setSilToISO639CodesUrl(String url) {
        this.silToISO639CodesUrl = url;
    }
            
    /**
     * Switch to external configuration.<br><br>
     * 
     * In addition to the definition of the configuration by the packaged in the
     * {@literal VloConfig.xml} file, you can configure the web application by
     * means of an XML file that resides outside the package. By letting a
     * parameter named<br><br>
     *
     * externalConfig<br><br>
     * 
     * in the context reference an XML file similar to the packaged one, the
     * parameters defined in this file will override the packaged parameters.
     * Please note that the use of an external XML file is not
     * compulsory.<br><br>
     *
     * Another way to externally configure the web application is to define
     * parameters by including them in the context fragment not via an XML file,
     * but directly. At the moment, only the packaged <br><br>
     * 
     * solrUrl<br><br>
     * 
     * parameter can be overridden in this way.
     * 
     * @param config static configuration 
     *
     * @return the static WebAppConfig member
     */
    public static VloConfig switchToExternalConfig(ServletContext context) {
              
        // assume that there is no file outside the package defining parameters

        boolean externalConfig = false;
        
        // check for a reference to of such a file

        String fileName;
        fileName = context.getInitParameter("externalConfig");
                
        if (fileName == null) {
            // no external configuration file
        } else {
            config = (VloConfig) read(fileName, config);
        }
        
        /**
         * In addition to modifications via an external configuration file,
         * check if the current configuration needs to be modified because of a
         * parameter defined in the context directly.
         */        
        String url = context.getInitParameter("solrUrl");
        
        if (url == null){
            // no overruling parameter in the context 
        } else
        {
            // overrule the current value of solrUrl

            VloConfig.get().setSolrUrl(url);
        }
        
        // return the current configuration, modified or not

        return config;
    }   
}