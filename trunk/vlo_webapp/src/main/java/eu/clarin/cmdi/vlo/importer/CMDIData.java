package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CMDIData {
    private final static Logger LOG = LoggerFactory.getLogger(CMDIData.class);
    private static final String METADATA_TYPE = "Metadata";
    private static final String DATA_RESOURCE_TYPE = "Resource";
    private static final String SEARCH_SERVICE_TYPE = "SearchService";

    private String id;
    private List<Resource> metaDataResources = new ArrayList<Resource>();
    private SolrInputDocument doc;
    private List<Resource> dataResources = new ArrayList<Resource>();
    private List<Resource> searchResources = new ArrayList<Resource>();

    public SolrInputDocument getSolrDocument() {
        return doc;
    }

    public void addDocField(String name, String value, boolean caseInsensitive) {
        if (FacetConstants.FIELD_ID.equals(name)) {
            setId(value.trim());
        } else {
            handleDocField(name, value, caseInsensitive);
        }
    }

    private void handleDocField(String name, String value, boolean caseInsensitive) {
        if (doc == null) {
            doc = new SolrInputDocument();
        }
        if (value != null && !value.trim().isEmpty()) {
            if (caseInsensitive) {
                value = value.toLowerCase();
            }
            Collection<Object> fieldValues = doc.getFieldValues(name);
            if (fieldValues == null || !fieldValues.contains(value)) {
                doc.addField(name, value);
            } //ignore double values don't add them
        }
    }

    public List<Resource> getDataResources() {
        return dataResources;
    }

    public List<Resource> getMetadataResources() {
        return metaDataResources;
    }

    //TODO CLARIN-type search resources (CQL endpoints) are not dealth with yet.
    //You can use this method to get the list and add it to the solr somehow :)
    public List<Resource> getSearchResources() {
        return searchResources;
    }

    public void addResource(String resource, String type, String mimeType) {
        if (METADATA_TYPE.equals(type)) {
            metaDataResources.add(new Resource(resource,type, mimeType));
        } else if (DATA_RESOURCE_TYPE.equals(type)) {
            dataResources.add(new Resource(resource,type, mimeType));
        }else if (SEARCH_SERVICE_TYPE.equals(type)){
            searchResources.add(new Resource(resource,type, mimeType));
        } else {
            LOG.warn("Found unsupported resource it will be ignored: type=" + type + ", name=" + resource);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
