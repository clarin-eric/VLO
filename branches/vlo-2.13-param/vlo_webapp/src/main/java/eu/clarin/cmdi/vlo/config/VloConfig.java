package eu.clarin.cmdi.vlo.config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletContext;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;

/**
 * Web application configuration<br><br>
 *
 * A parameter that is part of the configuration of the VLO web application can
 * be of two types: it is either a parameter that is defined from within the
 * application, an application parameter, or it is a parameter that is defined
 * in the context in which the application will live as a {@literal servlet}.
 * Application parameters reside in the {@literal WebAppConfig.xml} file, while
 * {@literal servlet} context parameters are part of the Tomcat server
 * configuration. <br><br>
 *
 * An application parameter is defined an XML file in the resources directory of
 * the application package. For every application parameter, the WebApplication
 * class contains a member that is annotated according to the Simple framework
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
 * WebAppConfig.open();<br><br>
 *
 * the parameter itself is accessed by<br><br>
 *
 * WebAppConfig.open().getParameterMember();<br><br>
 *
 * If you want to add a type of member that is not included in the class yet,
 * refer to the Simple framework's specification.<br><br>
 *
 * A context parameter also resides in an XML file. For more information on the
 * location and the format of this file, please refer to the Apache Tomcat
 * configuration reference.<br><br>
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
     * Make the configuration statically accessible<br><br>
     *
     * Both the Simple framework and the methods in the web application need to
     * access the configuration. Access is granted by defining a member holding
     * the configuration, that is, by defining a member of type WebAppConfig.
     */
    private static VloConfig config = null;

    /**
     * kj: change the annotation. Instead of opening a context, it is now a 
     * matter of initializing it. Make a new method for referencing.
     * 
     * Open a static context of WebAppConfig members, and assign values to
     * them.<br><br>
     * 
     * The web application can access a parameter by invoking one of the get or
     * set methods defined below. Because these methods return a non-static
     * value, while WebAppConfig on the other hand denotes a static
     * context,<br><br>
     *
     * WebAppConfig.getParameterMember()<br><br>
     *
     * for example, would not be valid. On encountering open() however, a new
     * static context is opened, and from that, getParameterMember() can be
     * invoked:<br><br>
     *
     * WebAppConfig.open().getParameterMember()<br><br>
     * 
     * @param fileName
     *
     * @return the web application configuration in a new static context
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
     * kj: add comment, much in the same way as the annotation of the WepApp
     * method.
     * 
     * In this method, exceptions to the normal web application context can 
     * be made.
     * 
     * @param fileName
     * 
     * @return 
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
     * kj: this is the new get context method
     * 
     * @return 
     */
    public static VloConfig get (){
        return config;
    }

    /**
     * Web application parameter members<br><br>
     *
     * Initialize a member corresponding to application parameters with an empty
     * string at least, for this will allow them to be linearized to
     * corresponding elements in the XML file.
     * 
     * Please refer to the general VLO documentation for a description of the
     * member parameters.
     */
    @Element // annotation directive as defined by Simple
    private String vloHomeLink = "";
    
    @Element
    private String solrUrl = "";
    // In the XML file, the value of the parameter is expanded by Maven.
    
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
     * {@literal <facetFields length="3">}<br> {@literal    <facetField>}<br>
     * {@literal       fieldOne}<br> {@literal    </facetField>}<br>
     * {@literal    <facetField>}<br> {@literal       fieldTwo}<br>
     * {@literal    </facetField>}<br> {@literal    <facetField>}<br>
     * {@literal       fieldThree}<br> {@literal    </facetField>}<br>
     * {@literal </facetFields>}<br><br>
     *
     * To let Spring now it has to interpret the facetFields element as an
     * array, use the ElementArray directive. Use the directive to let Spring
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
    public void setVloHomeLink(String link) {
        this.vloHomeLink = link;
    }

    /**
     * Get the SolrUrl parameter<br><br>
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
     * Get the ProfileSchemaUrl by profileId parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @return the parameter
     */
    public String getComponentRegistryProfileSchema(String id) {
        return profileSchemaUrl.replace("${PROFILE_ID}", id);
    }

    /**
     * Set the ProfileSchemaUrl parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @param profileId the parameter
     */
    public void setProfileSchemaUrl(String url) {
        this.profileSchemaUrl = url;
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
    public void setComponentRegistryRESTURL(String url) {
        this.componentRegistryRESTURL = url;
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
    public void setHandleServerUrl(String url) {
        this.handleServerUrl = url;
    }

    /**
     * Set the IMDIBrowserUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param imdiBrowserUrl the parameter
     */
    public void setIMDIBrowserUrl(String url) {
        this.imdiBrowserUrl = url;
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
    public void setFederatedContentSearchUrl(String url) {
        this.FederatedContentSearchUrl = url;
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
    public void setNationalProjectMapping(String mapping) {
        this.nationalProjectMapping = mapping;
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
    public void setFacetFields(String[] fields) {
        this.facetFields = fields;
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
    public void setCountryComponentUrl(String url) {
        this.countryComponentUrl = url;
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
    public void setLanguage2LetterCodeComponentUrl(String url) {
        this.language2LetterCodeComponentUrl = url;
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
    public void setLanguage3LetterCodeComponentUrl(String url) {
        this.language3LetterCodeComponentUrl = url;
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
    public void setSilToISO639CodesUrl(String url) {
        this.silToISO639CodesUrl = url;
    }
            
    /**
     *
     * kj: repair annotation
     * 
     * Contrary to Simple, the web application's context parameters are not
     * retrieved by annotation. Get them by invoking a local method.
     *
     * Add properties of the {@literal servlet's} context<br><br>
     *  
     * Keep the properties in the static context of the WebAppConfig class, next
     * to the members representing the values in WebAppConfig.xml file.<br><br>
     *
     * @param config static configuration 
     *
     * @return the static WebAppConfig member
     */
    public static VloConfig switchToExternalConfig(ServletContext context) {

        // retrieve parameter valies from the servlet context

        String fileName;
        fileName = context.getInitParameter("externalConfig");
        
        if (fileName == null) {
            // no external config
        } else {
            config = (VloConfig) read(fileName, config);
        }

        return config;
    }
    
    /**
     * {@literal Servlet} context members<br><br>
     *
     * The following defines the members corresponding to {@servlet} context
     * parameters.
     */    
}
