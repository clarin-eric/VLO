package eu.clarin.cmdi.vlo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * see applicationContext.xml resource for proper values.
 * 
 **/
public final class Configuration {

    private final static Configuration INSTANCE = new Configuration();

    public static final String CONFIG_FILE = "applicationContext.xml";
    private final static String PROFILE_ID_PLACEHOLDER = "{PROFILE_ID}";
    private String solrUrl;

    private String imdiBrowserUrl;// = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=";

    private String[] facetFields;// = new String[] { "origin", "organisation", "continent", "genre", "country", "subject", "language" };

    private String handleServerUrl; //"http://hdl.handle.net/"

    /**
     * Default values you can overwrite in applicationContext.xml: e.g. <property name="countryComponentUrl" value="" />
     */
    private String countryComponentUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438104/xml";
    private String language2LetterCodeComponentUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438109/xml";
    private String language3LetterCodeComponentUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xml";
    private String silToISO639CodesUrl = "http://www.clarin.eu/cmd/xslt/sil_to_iso6393.xml";
    private String profileSchemaUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/" + PROFILE_ID_PLACEHOLDER
            + "/xsd";

    private String vloHomeLink = "http://www.clarin.eu/vlo";//default can be overridden in xml

    private Configuration() {
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public void setSolrUrl(String solrUrl) {
        this.solrUrl = solrUrl;
    }

    public String getSolrUrl() {
        return solrUrl;
    }

    public void setIMDIBrowserUrl(String imdiBrowserUrl) {
        this.imdiBrowserUrl = imdiBrowserUrl;
    }

    public String getIMDIBrowserUrl(String handle) {
        String result;
        try {
            result = imdiBrowserUrl + URLEncoder.encode(handle, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = imdiBrowserUrl + handle;
        }
        return result;
    }

    public String[] getFacetFields() {
        return facetFields;
    }

    public void setFacetFields(String[] facetFields) {
        this.facetFields = facetFields;
    }

    public String getHandleServerUrl() {
        return handleServerUrl;
    }

    public void setHandleServerUrl(String handleServerUrl) {
        this.handleServerUrl = handleServerUrl;
    }

    public String getCountryComponentUrl() {
        return countryComponentUrl;
    }

    public void setCountryComponentUrl(String countryComponentUrl) {
        this.countryComponentUrl = countryComponentUrl;
    }

    public String getLanguage2LetterCodeComponentUrl() {
        return language2LetterCodeComponentUrl;
    }

    public void setLanguage2LetterCodeComponentUrl(String language2LetterCodeComponentUrl) {
        this.language2LetterCodeComponentUrl = language2LetterCodeComponentUrl;
    }

    public String getLanguage3LetterCodeComponentUrl() {
        return language3LetterCodeComponentUrl;
    }

    public void setLanguage3LetterCodeComponentUrl(String language3LetterCodeComponentUrl) {
        this.language3LetterCodeComponentUrl = language3LetterCodeComponentUrl;
    }

    public String getSilToISO639CodesUrl() {
        return silToISO639CodesUrl;
    }

    public void setSilToISO639CodesUrl(String silToISO639CodesUrl) {
        this.silToISO639CodesUrl = silToISO639CodesUrl;
    }

    public String getVloHomeLink() {
        return vloHomeLink;
    }

    public void setVloHomeLink(String vloHomeLink) {
        this.vloHomeLink = vloHomeLink;
    }

    public String getComponentRegistryProfileSchema(String profileId) {
        return profileSchemaUrl.replace(PROFILE_ID_PLACEHOLDER, profileId);
    }

    public void setProfileSchemaUrl(String profileSchemaUrl) {
        this.profileSchemaUrl = profileSchemaUrl;
    }
    

}
