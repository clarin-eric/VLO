package eu.clarin.cmdi.vlo.config;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * VLO configuration
 *
 * @author keeloo, twagoo
 */
/**
 * @author @author Wolfgang Walter SAUER (wowasa)
 *         &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
@XmlRootElement(name = "VloConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class VloConfig {

    public static final String LANGUAGE_LINK_TEMPLATE_LANGUAGE_CODE_PLACEHOLDER = "{}";
    
    // resources included into vlo-commons during build from vlo-mapping project
    
    public static final String DEFAULT_FACET_CONCEPTS_RESOURCE_FILE = "/vlo-mapping/mapping/facetConcepts.xml"; 
    public static final String DEFAULT_FACETS_CONFIG_RESOURCE_FILE = "/vlo-mapping/config/facetsConfiguration.xml"; 

    /*
     * VLO application parameter members<br><br>
     *
     * Please Initialise the annotated members in a proper way. This will allow them
     * to be linearised to corresponding elements in an XML file.
     * 
     * Please refer to the general VLO documentation for a description of the member
     * parameters.
     */
    // page cache related parameters
    private int pagesInApplicationCache = 0;

    private int sessionCacheSize = 0;

    // data base related parameters
    private String solrUrl = "";

    // directive for Simple
    private boolean deleteAllFirst = false;

    private int maxDocsInList = 0;

    private int minDocsInSolrQueue = 0;

    // (required = false)
    private int solrTimeOut = 0;

    // Solr authentication
    private String solrUserReadOnly;
    private String solrUserReadOnlyPass;
    private String solrUserReadWrite;
    private String solrUserReadWritePass;

    private boolean vloExposureEnabled;
    // Postgresql authentication
    private String vloExposureDbName;
    private String vloExposurePort;
    private String vloExposureHost;
    private String vloExposureUsername;
    private String vloExposurePassword;

    @XmlElementWrapper(name = "dataRoots")
    @XmlElement(name = "DataRoot")
    private List<DataRoot> dataRoot;

    private int maxFileSize = 0;

    private int maxDaysInSolr = 0;

    // mapping
    // (required = false)
    private String facetConceptsFile = "";
    
    private String facetsConfigFile = "";

    private boolean printMapping = false;

    private String organisationNamesUrl;

    private String languageNameVariantsUrl;

    private String licenseAvailabilityMapUrl;

    private String licenseURIMapUrl;

    private String licenseTypeMapUrl;

    private String valueMappingsFile = "";

    private String countryComponentUrl = "";

    private String language2LetterCodeComponentUrl = "";

    private String language3LetterCodeComponentUrl = "";

    private String silToISO639CodesUrl = "";

    // services
    @XmlElement(name = "FederatedContentSearchUrl")
    private String federatedContentSearchUrl = "";

    private boolean useHandleResolver = false;

    @XmlElement
    private String profileSchemaUrl = "";

    private String componentRegistryRESTURL = "";

    private String handleServerUrl = "";

    private String languageLinkPrefix = "";

    private String languageLinkTemplate = "";

    private String conceptRegistryUrl = "";

    private String vocabularyRegistryUrl = "";

    private String homeUrl = "";

    private String helpUrl = "";

    private String feedbackFromUrl = "";

    private String vcrSubmitEndpoint;

    private Long vcrMaximumItemsCount = 0L;

    private boolean showResultScores = false;

    private boolean processHierarchies = true;

    private int fileProcessingThreads = 2;

    private int solrThreads = 2;
    
    private String linkCheckerDbConnectionString;

    private String linkCheckerDbUser;

    private String linkCheckerDbPassword;

    private String centreRegistryCentresListJsonUrl;

    private String centreRegistryOaiPmhEndpointsListJsonUrl;

    private String otherProvidersMarkupFile;

    private int availabilityStatusUpdaterBatchSize = 50;
    
    private int maxNumberOfFacetsToShow = 10;
    
    private boolean enableFcsLinks;
    
    private String webAppLocale;
    
    private DataSetStructuredData dataSetStructuredData;

    @XmlJavaTypeAdapter(XmlFieldAdapter.class)
    private Map<String, String> fields;

    @XmlJavaTypeAdapter(XmlFieldAdapter.class)
    @XmlElement(name = "deprecatedFields")
    private Map<String, String> deprecatedFields;

    @XmlElementWrapper(name = "signatureFields")
    private List<String> signatureField;

    private int hideSecondaryFacetsLimit = 7;

    // (required = false)
    private String cqlEndpointFilter = "";

    // (required = false)
    private String cqlEndpointAlternative = "";

    private URI configLocation;

    private String lrSwitchboardBaseUrl = "https://switchboard.clarin.eu/";
    
    private String lrSwitchboardPopupScriptUrl="https://switchboard.clarin.eu/popup/switchboardpopup.js";
    
    private String lrSwitchboardPopupStyleUrl="https://switchboard.clarin.eu/popup/switchboardpopup.css";
    
    private boolean lrSwitchboardPopupEnabled = true;

    /**
     * Get and set methods for web application parameter members<br>
     * <br>
     *
     * By using a get or set method, you can apply an operation to a parameter here
     * without the need to make changes in different parts of the application.
     */
    /**
     * Get the value of the pagesInApplicationCache parameter<br>
     * <br>
     *
     * The parameter represents the number of pages that Wicket will allow to be
     * stored in the application's cache.
     *
     * @return the value
     */
    public int getPagesInApplicationCache() {
        return pagesInApplicationCache;
    }

    /**
     * Set the value of the pagesInApplicationCache parameter<br>
     * <br>
     *
     * The parameter represents the number of pages that Wicket will allow to be
     * stored in the application's cache.
     *
     * @param param the value
     */
    public void setPagesInApplicationCache(int param) {
        pagesInApplicationCache = param;
    }

    /**
     * Get the value of the sessionCacheSize parameter<br>
     * <br>
     *
     * The parameter represents the size in kilobytes of the session page cache.
     *
     * @return the value
     */
    public int getSessionCacheSize() {
        return sessionCacheSize;
    }

    /**
     * Set the value of the sessionCacheSize parameter<br>
     * <br>
     *
     * The parameter represents the size in megabytes of the session page cache.
     *
     * @param param the value
     */
    public void setSessionCacheSize(int param) {
        sessionCacheSize = param;
    }

    /**
     * Get the value of the deleteAllFirst parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public boolean getDeleteAllFirst() {
        return deleteAllFirst;
    }

    /**
     * Set the value of the deleteAllFirst parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setDeleteAllFirst(boolean param) {
        deleteAllFirst = param;
    }

    /**
     * Get the value of the maxDocsInList parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public int getMaxDocsInList() {
        return maxDocsInList;
    }

    /**
     * Set the value of the maxDocsInList parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setMaxDocsInList(int param) {
        maxDocsInList = param;
    }

    /**
     * Get the value of the minDocsInSolrQueue parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public int getMinDocsInSolrQueue() {
        return minDocsInSolrQueue;
    }

    /**
     * Set the value of the minDocsInSolrQueue parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setMinDocsInSolrQueue(int param) {
        minDocsInSolrQueue = param;
    }

    /**
     * Get the value of the solrTimeOut parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public int getSolrTimeOut() {
        return solrTimeOut;
    }

    /**
     * Set the value of the solrTimeOut parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setSolrTimeOut(int param) {
        solrTimeOut = param;
    }

    /**
     * @return the solrUserReadOnly
     */
    public String getSolrUserReadOnly() {
        return solrUserReadOnly;
    }

    /**
     * @param solrUserReadOnly the userReadOnlyName to set
     */
    public void setSolrUserReadOnly(String solrUserReadOnly) {
        this.solrUserReadOnly = solrUserReadOnly;
    }

    /**
     * @return the solrUserReadOnlyPass
     */
    public String getSolrUserReadOnlyPass() {
        return solrUserReadOnlyPass;
    }

    /**
     * @param solrUserReadOnlyPass the solrUserReadOnlyPass to set
     */
    public void setSolrUserReadOnlyPass(String solrUserReadOnlyPass) {
        this.solrUserReadOnlyPass = solrUserReadOnlyPass;
    }

    /**
     * @return the solrUserReadWrite
     */
    public String getSolrUserReadWrite() {
        return solrUserReadWrite;
    }

    /**
     * @param solrUserReadWrite the solrUserReadWrite to set
     */
    public void setSolrUserReadWrite(String solrUserReadWrite) {
        this.solrUserReadWrite = solrUserReadWrite;
    }

    /**
     * @return the solrUserReadWritePass
     */
    public String getSolrUserReadWritePass() {
        return solrUserReadWritePass;
    }

    /**
     * @param solrUserReadWritePass the solrUserReadWritePass to set
     */
    public void setSolrUserReadWritePass(String solrUserReadWritePass) {
        this.solrUserReadWritePass = solrUserReadWritePass;
    }

    /**
     * @return the vloExposureEnabled
     */
    public boolean isVloExposureEnabled() {
        return vloExposureEnabled;
    }

    /**
     * @param vloExposureEnabled the vloExposureEnabled to set
     */
    public void isVloExposureEnabled(boolean vloExposureEnabled) {
        this.vloExposureEnabled = vloExposureEnabled;
    }

    /**
     * @return the vloExposureDbName
     */
    public String getVloExposureDbName() {
        return this.vloExposureDbName;
    }

    /**
     * @param vloExposureDbName the vloExposureDbName to set
     */
    public void setVloExposureDbName(String vloExposureDbName) {
        this.vloExposureDbName = vloExposureDbName;
    }

    /**
     * @return the vloExposurePort
     */
    public String getVloExposurePort() {
        return vloExposurePort;
    }

    /**
     * @param vloExposurePort the vloExposurePort to set
     */
    public void setVloExposurePort(String vloExposurePort) {
        this.vloExposurePort = vloExposurePort;
    }

    /**
     * @return the vloExposureHost
     */
    public String getVloExposureHost() {
        return vloExposureHost;
    }

    /**
     * @param vloExposureHost the vloExposureHost to set
     */
    public void setVloExposureHost(String vloExposureHost) {
        this.vloExposureHost = vloExposureHost;
    }

    /**
     * @return the vloExposureUsername
     */
    public String getVloExposureUsername() {
        return vloExposureUsername;
    }

    /**
     * @param vloExposureUsername the vloExposureUsername to set
     */
    public void setVloExposureUsername(String vloExposureUsername) {
        this.vloExposureUsername = vloExposureUsername;
    }

    /**
     * @return the vloExposurePassword
     */
    public String getVloExposurePassword() {
        return vloExposurePassword;
    }

    /**
     * @param vloExposurePassword the vloExposurePassword to set
     */
    public void setVloExposurePassword(String vloExposurePassword) {
        this.vloExposurePassword = vloExposurePassword;
    }

    /**
     * Get the value of the maxFileSize parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public int getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Set the value of the maxFileSize parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setMaxFileSize(int param) {
        maxFileSize = param;
    }

    /**
     * Get the value of the maxDaysInSolr parameter<br>
     * <br>
     *
     * If the parameter is larger than 0, it denotes the maximal number of days that
     * a document can remain in the database.
     *
     * @return the value
     */
    public int getMaxDaysInSolr() {
        return maxDaysInSolr;
    }

    /**
     * Set the value of the maxDaysInSolr parameter<br>
     * <br>
     *
     * If the parameter is larger than 0, it denotes the maximal number of days that
     * a document can remain in the database.
     *
     * @param param the value
     */
    public void setMaxDaysInSolr(int param) {
        maxDaysInSolr = param;
    }

    /**
     * Get the value of the useHandleResolver parameter<br>
     * <br>
     *
     * The parameter can be used to reject meta data files that exceed some maximum
     * size.
     *
     * @return the value
     */
    public boolean getUseHandleResolver() {
        return useHandleResolver;
    }

    /**
     * Set the value of the useHandleResolver parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setUseHandleResolver(boolean param) {
        useHandleResolver = param;
    }

    /**
     * Get the list of dataRoots parameters<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public List<DataRoot> getDataRoots() {
        return dataRoot;
    }

    /**
     * Set the value of a list of dataRoots parameters<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setDataRoots(List<DataRoot> param) {
        dataRoot = param;
    }

    /**
     * Get the value of the printMapping parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public boolean printMapping() {
        return printMapping;
    }

    /**
     * Set the value of the printMapping parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setPrintMapping(boolean param) {
        printMapping = param;
    }

    /**
     * Get the value of the VloHomeLink parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getHomeUrl() {
        return homeUrl;
    }

    /**
     * Set the value of the VloHomeLink parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setHomeUrl(String param) {
        homeUrl = param;
    }

    /**
     * Get the value of the helpUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getHelpUrl() {
        return helpUrl;
    }

    /**
     * Set the value of the helpUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setHelpUrl(String param) {
        helpUrl = param;
    }

    /**
     * Get the value of the SolrUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getSolrUrl() {
        return solrUrl;
    }

    /**
     * Set the value of the SolrUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setSolrUrl(String param) {
        solrUrl = param;
    }

    /**
     * Get the value of the facetConceptsFile parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getFacetConceptsFile() {
        return facetConceptsFile;
    }

    /**
     * Set the value of the facetConceptsFile parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setFacetConceptsFile(String param) {
        facetConceptsFile = param;
    }

    public String getFacetsConfigFile() {
        return facetsConfigFile;
    }

    public void setFacetsConfigFile(String facetsConfigFile) {
        this.facetsConfigFile = facetsConfigFile;
    }

    /**
     * Get the value of the ProfileSchemaUrl by profileId parameter<br>
     * <br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @return the value
     */
    public String getComponentRegistryProfileSchema(String id) {
        return profileSchemaUrl.replace("{PROFILE_ID}", id);
    }

    /**
     * Set the value of the ProfileSchemaUrl parameter<br>
     * <br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @param param the value
     */
    public void setComponentRegistryProfileSchema(String param) {
        profileSchemaUrl = param;
    }

    /**
     * Get the value of the ComponentRegisteryRESTURL parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getComponentRegistryRESTURL() {
        return componentRegistryRESTURL;
    }

    /**
     * Set the value of the ComponentRegisteryRESTURL parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setComponentRegistryRESTURL(String param) {
        componentRegistryRESTURL = param;
    }

    /**
     * Get the value of the HandleServerUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getHandleServerUrl() {
        return handleServerUrl;
    }

    /**
     * Set the value of the HandleServerUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setHandleServerUrl(String param) {
        handleServerUrl = param;
    }

    /**
     * Set the value of the ConceptRegistryUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setConceptRegistryUrl(String param) {
        conceptRegistryUrl = param;
    }

    /**
     * Get the value of the ConceptRegistryUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getConceptRegistryUrl() {
        return conceptRegistryUrl;
    }

    /**
     * Set the value of the VocabularyRegistryUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setVocabularyRegistryUrl(String param) {
        vocabularyRegistryUrl = param;
    }

    /**
     * Get the value of the VocabularyRegistryUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getVocabularyRegistryUrl() {
        return vocabularyRegistryUrl;
    }

    /**
     * Get the value of the languageLinkPrefix parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     * @see #LANGUAGE_LINK_TEMPLATE_LANGUAGE_CODE_PLACEHOLDER
     */
    public String getLanguageLinkTemplate() {
        return languageLinkTemplate;
    }

    /**
     * Set the value of the languageLinkPrefix parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     * @see #LANGUAGE_LINK_TEMPLATE_LANGUAGE_CODE_PLACEHOLDER
     */
    public void setLanguageLinkTemplate(String param) {
        languageLinkTemplate = param;
    }

    /**
     * Get the value of the feedbackFromUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getFeedbackFromUrl() {
        return feedbackFromUrl;
    }

    /**
     * Set the value of the feedbackFromUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setFeedbackFromUrl(String param) {
        feedbackFromUrl = param;
    }

    public String getVcrSubmitEndpoint() {
        return vcrSubmitEndpoint;
    }

    public void setVcrSubmitEndpoint(String vcrSubmitEndpoint) {
        this.vcrSubmitEndpoint = vcrSubmitEndpoint;
    }

    public Long getVcrMaximumItemsCount() {
        return vcrMaximumItemsCount;
    }

    public void setVcrMaximumItemsCount(Long vcrMaximumItemsCount) {
        this.vcrMaximumItemsCount = vcrMaximumItemsCount;
    }

    /**
     * Get the value of the FederatedContentSearchUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getFederatedContentSearchUrl() {
        return federatedContentSearchUrl;
    }

    /**
     * Set the value of the FederatedContentSearchUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setFederatedContentSearchUrl(String param) {
        federatedContentSearchUrl = param;
    }

    public List<String> getSignatureFieldKeys() {
        return this.signatureField;
    }

    public List<String> getSignatureFieldNames() {
        return this.signatureField.stream().map(key -> this.fields.get(key)).collect(Collectors.toList());
    }

    public void setSignatureFields(List<String> signatureField) {
        this.signatureField = signatureField;
    }

    /**
     * Get the value of the CountryComponentUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getCountryComponentUrl() {
        return countryComponentUrl;
    }

    /**
     * Set the value of the CountryComponentUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setCountryComponentUrl(String param) {
        countryComponentUrl = param;
    }

    /**
     * Get the value of the Language2LetterCodeComponentUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getLanguage2LetterCodeComponentUrl() {
        return language2LetterCodeComponentUrl;
    }

    /**
     * Set the value of the Language2LetterCodeComponentUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setLanguage2LetterCodeComponentUrl(String param) {
        language2LetterCodeComponentUrl = param;
    }

    /**
     * Get the value of the Language3LetterCodeComponentUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getLanguage3LetterCodeComponentUrl() {
        return language3LetterCodeComponentUrl;
    }

    /**
     * Set the value of the Language3LetterCodeComponentUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setLanguage3LetterCodeComponentUrl(String param) {
        language3LetterCodeComponentUrl = param;
    }

    /**
     * Get the value of the SilToISO639CodesUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getSilToISO639CodesUrl() {
        return silToISO639CodesUrl;
    }

    /**
     * Set the value of the SilToISO639CodesUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setSilToISO639CodesUrl(String param) {
        silToISO639CodesUrl = param;
    }

    /**
     * Get the value of the organisationNamesUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getOrganisationNamesUrl() {
        return organisationNamesUrl;
    }

    /**
     * Set the value of the organisationNamesUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setOrganisationNamesUrl(String param) {
        organisationNamesUrl = param;
    }

    /**
     * Get the value of the languageNameVariantsUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getLanguageNameVariantsUrl() {
        return languageNameVariantsUrl;
    }

    /**
     * Set the value of the languageNameVariantsUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setLanguageNameVariantsUrl(String param) {
        languageNameVariantsUrl = param;
    }

    /**
     * Get the value of the licenseAvailabilityMapUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getLicenseAvailabilityMapUrl() {
        return licenseAvailabilityMapUrl;
    }

    /**
     * Set the value of the licenseAvailabilityMapUrl parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setLicenseAvailabilityMapUrl(String param) {
        licenseAvailabilityMapUrl = param;
    }

    public String getLicenseURIMapUrl() {
        return licenseURIMapUrl;
    }

    public void setLicenseURIMapUrl(String licenseURIMapUrl) {
        this.licenseURIMapUrl = licenseURIMapUrl;
    }

    public String getLicenseTypeMapUrl() {
        return licenseTypeMapUrl;
    }

    public void setLicenseTypeMapUrl(String licenseURIMapUrl) {
        this.licenseTypeMapUrl = licenseURIMapUrl;
    }

    /**
     * Get the value of the cqlEndpointFilter parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getCqlEndpointFilter() {
        return cqlEndpointFilter;
    }

    /**
     * Set the value of the cqlEndpointFilter parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setCqlEndpointFilter(String param) {
        cqlEndpointFilter = param;
    }

    /**
     * Get the value of the cqlEndpointAlternative parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @return the value
     */
    public String getCqlEndpointAlternative() {
        return cqlEndpointAlternative;
    }

    /**
     * Set the value of the cqlEndpointAlternative parameter<br>
     * <br>
     *
     * For a description of the parameter, refer to the general VLO documentation.
     *
     * @param param the value
     */
    public void setCqlEndpointAlternative(String param) {
        cqlEndpointAlternative = param;
    }

    /**
     * Sets the originating file for the current configuration
     *
     * @param configFile the file this config was read from
     */
    public void setConfigLocation(URI configFile) {
        this.configLocation = configFile;
    }

    /**
     *
     * @return the file this config was read from. may be null!
     */
    public URI getConfigLocation() {
        return configLocation;
    }

    public void setShowResultScores(boolean showResultScores) {
        this.showResultScores = showResultScores;
    }

    public boolean isShowResultScores() {
        return showResultScores;
    }

    /**
     * Get the value of processHierarchies
     *
     * @return the value of processHierarchies
     */
    public boolean isProcessHierarchies() {
        return processHierarchies;
    }

    /**
     * Set the value of processHierarchies
     *
     * @param processHierarchies new value of processHierarchies
     */
    public void setProcessHierarchies(boolean processHierarchies) {
        this.processHierarchies = processHierarchies;
    }

    private List<FieldValueDescriptor> availabilityValue;

    @XmlElementWrapper(name = "availability")
    @XmlElement(name = "availabilityValue")
    public List<FieldValueDescriptor> getAvailabilityValues() {
        return availabilityValue;
    }

    public void setAvailabilityValues(List<FieldValueDescriptor> availabilityValues) {
        this.availabilityValue = availabilityValues;
    }

    public String getLrSwitchboardBaseUrl() {
        return lrSwitchboardBaseUrl;
    }

    public void setLrSwitchboardBaseUrl(String lrSwitchboardBaseUrl) {
        this.lrSwitchboardBaseUrl = lrSwitchboardBaseUrl;
    }

    public int getHideSecondaryFacetsLimit() {
        return hideSecondaryFacetsLimit;
    }

    public void setHideSecondaryFacetsLimit(int hideSecondaryFacetsLimit) {
        this.hideSecondaryFacetsLimit = hideSecondaryFacetsLimit;
    }

    public int getFileProcessingThreads() {
        return fileProcessingThreads;
    }

    public void setFileProcessingThreads(int fileProcessingThreads) {
        this.fileProcessingThreads = fileProcessingThreads;
    }

    public int getSolrThreads() {
        return solrThreads;
    }

    public void setSolrThreads(int solrThreads) {
        this.solrThreads = solrThreads;
    }

    public String getValueMappingsFile() {
        return this.valueMappingsFile;
    }

    public void setValueMappingsFile(String valueMappingsFile) {
        this.valueMappingsFile = valueMappingsFile;
    }

    /**
     * @return the fields
     */
    public Map<String, String> getFields() {
        return fields;
    }

    /**
     * @param fields the fields to set
     */
    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    /**
     * @return the dFields
     */
    public Map<String, String> getDeprecatedFields() {
        return deprecatedFields;
    }

    /**
     * @param dFields the dFields to set
     */
    public void setDeprecatedFields(Map<String, String> deprecatedFields) {
        this.deprecatedFields = deprecatedFields;
    }

    public String getLinkCheckerDbConnectionString() {
        return linkCheckerDbConnectionString;
    }

    public void setLinkCheckerDbConnectionString(String linkCheckerDbConnectionString) {
        this.linkCheckerDbConnectionString = linkCheckerDbConnectionString;
    }

    public String getLinkCheckerDbUser() {
        return linkCheckerDbUser;
    }

    public void setLinkCheckerDbUser(String linkCheckerDbUser) {
        this.linkCheckerDbUser = linkCheckerDbUser;
    }

    public String getLinkCheckerDbPassword() {
        return linkCheckerDbPassword;
    }

    public void setLinkCheckerDbPassword(String linkCheckerDbPassword) {
        this.linkCheckerDbPassword = linkCheckerDbPassword;
    }    

    public String getCentreRegistryCentresListJsonUrl() {
        return centreRegistryCentresListJsonUrl;
    }

    public void setCentreRegistryCentresListJsonUrl(String centreRegistryCentresListJsonUrl) {
        this.centreRegistryCentresListJsonUrl = centreRegistryCentresListJsonUrl;
    }

    public String getCentreRegistryOaiPmhEndpointsListJsonUrl() {
        return centreRegistryOaiPmhEndpointsListJsonUrl;
    }

    public void setCentreRegistryOaiPmhEndpointsListJsonUrl(String centreRegistryOaiPmhEndpointsListJsonUrl) {
        this.centreRegistryOaiPmhEndpointsListJsonUrl = centreRegistryOaiPmhEndpointsListJsonUrl;
    }

    public String getOtherProvidersMarkupFile() {
        return otherProvidersMarkupFile;
    }

    public void setOtherProvidersMarkupFile(String otherProvidersMarkupFile) {
        this.otherProvidersMarkupFile = otherProvidersMarkupFile;
    }

    public Integer getAvailabilityStatusUpdaterBatchSize() {
        return availabilityStatusUpdaterBatchSize;
    }

    public void setAvailabilityStatusUpdaterBatchSize(Integer availabilityStatusUpdaterBatchSize) {
        this.availabilityStatusUpdaterBatchSize = availabilityStatusUpdaterBatchSize;
    }

    public boolean isEnableFcsLinks() {
        return enableFcsLinks;
    }

    public void setEnableFcsLinks(boolean enableFcsLinks) {
        this.enableFcsLinks = enableFcsLinks;
    }

    public String getWebAppLocale() {
        return webAppLocale;
    }

    public void setWebAppLocale(String webAppLocale) {
        this.webAppLocale = webAppLocale;
    }

    public void setDataSetStructuredData(DataSetStructuredData dataSetStructuredData) {
        this.dataSetStructuredData = dataSetStructuredData;
    }

    public DataSetStructuredData getDataSetStructuredData() {
        return dataSetStructuredData;
    }

    public void setMaxNumberOfFacetsToShow(int maxNumberOfFacetsToShow) {
        this.maxNumberOfFacetsToShow = maxNumberOfFacetsToShow;
    }

    public int getMaxNumberOfFacetsToShow() {
        return maxNumberOfFacetsToShow;
    }

    public String getLrSwitchboardPopupScriptUrl() {
        return lrSwitchboardPopupScriptUrl;
    }

    public void setLrSwitchboardPopupScriptUrl(String lrSwitchboardPopupScriptUrl) {
        this.lrSwitchboardPopupScriptUrl = lrSwitchboardPopupScriptUrl;
    }

    public String getLrSwitchboardPopupStyleUrl() {
        return lrSwitchboardPopupStyleUrl;
    }

    public void setLrSwitchboardPopupStyleUrl(String lrSwitchboardPopupStyleUrl) {
        this.lrSwitchboardPopupStyleUrl = lrSwitchboardPopupStyleUrl;
    }

    public boolean isLrSwitchboardPopupEnabled() {
        return lrSwitchboardPopupEnabled;
    }

    public void setLrSwitchboardPopupEnabled(boolean lrSwitchboardPopupEnabled) {
        this.lrSwitchboardPopupEnabled = lrSwitchboardPopupEnabled;
    }

}
