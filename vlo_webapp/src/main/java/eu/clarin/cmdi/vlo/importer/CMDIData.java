package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

public class CMDIData {

    private static final String METADATA_TYPE = "Metadata";
    private String id;
    private List<String> resources = new ArrayList<String>();
    private SolrInputDocument doc;

    public SolrInputDocument getSolrDocument() {
        return doc;
    }

    public void addDocField(String name, String value) {
        if (doc == null) {
            doc = new SolrInputDocument();
        }
        doc.addField(name, value);
    }

    public List<String> getResources() {
        return resources;
    }

    public void addResource(String resource, String type) {
        if (METADATA_TYPE.equals(type)) {
            resources.add(resource);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
