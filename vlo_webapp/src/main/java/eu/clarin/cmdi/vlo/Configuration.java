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

    private String solrUrl;

    private String imdiBrowserUrl;// = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=";

    private String[] facetFields;// = new String[] { "origin", "organisation", "continent", "genre", "country", "subject", "language" };

    private String handleServerUrl; //"http://hdl.handle.net/"

    /**
     * Default value you can overwrite in applicationContext.xml: <property name="countryComponentUrl" value="" />
     */
    private String countryComponentUrl = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438104/xml";

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

}
