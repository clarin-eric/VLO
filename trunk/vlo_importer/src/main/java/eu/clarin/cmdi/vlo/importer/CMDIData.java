package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a document of cmdi data.
 * Quite a central class to the metadata importer process.
 */

public class CMDIData {
    private final static Logger LOG = LoggerFactory.getLogger(CMDIData.class);
    private static final String METADATA_TYPE = "Metadata";
    private static final String DATA_RESOURCE_TYPE = "Resource";
    private static final String SEARCH_SERVICE_TYPE = "SearchService";
    private static final String LANDING_PAGE_TYPE = "LandingPage";

    /**
     * The unique identifier of the cmdi file.
     */
    private String id;
    
    /**
     * The associated solr document (not send to the solr server yet)
     */
    private SolrInputDocument doc;

    // Lists for different types of resources.
    private final List<Resource> metaDataResources = new ArrayList<Resource>();
    private final List<Resource> dataResources = new ArrayList<Resource>();
    private final List<Resource> searchResources = new ArrayList<Resource>();
    private final List<Resource> landingPageResources = new ArrayList<Resource>();

    public SolrInputDocument getSolrDocument() {
        return doc;
    }

    /**
     * Sets a field in the doc to a certain value. Well, at least calls another 
     * (private) method that actually does this.
     * @param name
     * @param value
     * @param caseInsensitive
     */
    public void addDocField(String name, String value, boolean caseInsensitive) {
        if (FacetConstants.FIELD_ID.equals(name)) {
            setId(value.trim());
        } else {
            handleDocField(name, value, caseInsensitive);
        }
    }

    /**
     * Sets a field in the doc to a certain value.
     * Before adding checks for duplicates.
     * @param name
     * @param value
     * @param caseInsensitive
     */
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

    /**
     * Returns list of all search interfaces (preferably CQL interfaces)
     */
    public List<Resource> getSearchResources() {
        return searchResources;
    }

    /**
     * Return a list of landing page resources
     */
    public List<Resource> getLandingPageResources() {
        return landingPageResources;
    }

    /**
     * Processes a resource by adding it to the internal lists. Supports meta
     * data, data, search service and landing page type of resources. Emits a
     * warning if another type of resource is encountered (not allowed according
     * to the cmdi spec, but we try to be a tad robust).
     *
     * @param resource
     * @param type
     * @param mimeType
     */
    public void addResource(String resource, String type, String mimeType) {
        if (METADATA_TYPE.equals(type)) {
            metaDataResources.add(new Resource(resource,type, mimeType));
        } else if (DATA_RESOURCE_TYPE.equals(type)) {
            dataResources.add(new Resource(resource,type, mimeType));
        } else if (SEARCH_SERVICE_TYPE.equals(type)){
            searchResources.add(new Resource(resource,type, mimeType));
        } else if (LANDING_PAGE_TYPE.equals(type)){
            landingPageResources.add(new Resource(resource,type, mimeType));
        } else {
            LOG.warn("Found unsupported resource it will be ignored: type=" + 
                    type + ", name=" + resource);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
