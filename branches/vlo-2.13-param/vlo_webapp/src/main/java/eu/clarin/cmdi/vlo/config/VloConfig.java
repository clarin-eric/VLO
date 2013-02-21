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
     * for example, would not be valid. On encountering get() however, a new
     * static context is opened, and from that, getParameterMember() can be
     * invoked:<br><br>
     *
     * WebAppConfig.get().getParameterMember()<br><br>
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
     * VLO application parameter members<br><br>
     *
     * Initialize a member corresponding to application parameters with an empty
     * string at least, for this will allow them to be linearized to
     * corresponding elements in the XML file.
     * 
     * Please refer to the general VLO documentation for a description of the
     * member parameters.
     */
    
    /**
     * Flag to signal the records in the data to be deleted before the ingestion
     * starts.
     */
    @Element // directive for Simple
    private boolean deleteAllFirst = false;
    
    /**
     * Flag that leads to the printing of XPATH mappings encountered. Note: need
     * to be more specific on this.
     */
    @Element
    private boolean printMapping = false;
    
    /**
     * A list of data roots, that is: directories from which the importer
     * collects meta data. Note: need to elaborate on this.
     */
    @ElementList // directive for Simple
    private List<DataRoot> dataRoots;
    
    public List<DataRoot> getDataRoots() {
        return dataRoots;
    }
    
    @Element
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
     * Get and read methods for web application parameter members<br><br>
     *
     * By using a get or read method, you can apply an operation to a parameter
     * here, in the WebAppConfig class, without the need to make changes in
     * different parts of the application.
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
     * @param solrUrl the value
     */
    public String getSolrUrl() {
        return solrUrl;
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
