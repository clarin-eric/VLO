package eu.clarin.cmdi.vlo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * see applicationContext.xml resource for proper values.
 * 
 **/
public final class Configuration {
    
    private final static Configuration INSTANCE = new Configuration();

    private String solrUrl;

    private String imdiBrowserUrl;// = "http://corpus1.mpi.nl/ds/imdi_browser?openpath=";

    private String[] facetFields;// = new String[] { "origin", "organisation", "continent", "genre", "country", "subject", "language" };

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
}
