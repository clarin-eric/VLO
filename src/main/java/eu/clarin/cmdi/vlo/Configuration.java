package eu.clarin.cmdi.vlo;


public final class Configuration {

    private final static Configuration INSTANCE = new Configuration();

    private String solrUrl;

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

}
