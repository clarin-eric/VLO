package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.FacetConstants;

/**
 * Represents a document of CMDI data.
 */
public class CMDIData {

    private final static Logger LOG = LoggerFactory.getLogger(CMDIData.class);
    private static final String METADATA_TYPE = "Metadata";
    private static final String DATA_RESOURCE_TYPE = "Resource";
    private static final String SEARCH_SERVICE_TYPE = "SearchService";
    //* Definition of the string denoting the landing page type. */
    private static final String LANDING_PAGE_TYPE = "LandingPage";
    //* Definition of the string denoting the search page type. */
    private static final String SEARCH_PAGE_TYPE = "SearchPage";

    /**
     * The unique identifier of the cmdi file.
     */
    private String id;

    /**
     * The associated solr document (not send to the solr server yet)
     */
    private SolrInputDocument doc;

    // Lists for different types of resources.
    private final List<Resource> metaDataResources = new ArrayList<>();
    private final List<Resource> dataResources = new ArrayList<>();
    private final List<Resource> searchResources = new ArrayList<>();
    private final List<Resource> landingPageResources = new ArrayList<>();
    private final List<Resource> searchPageResources = new ArrayList<>();

    public SolrInputDocument getSolrDocument() {
        return doc;
    }

    /**
     * Sets a field in the doc to a certain value. Well, at least calls another
     * (private) method that actually does this.
     *
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
     * Sets a field in the doc to a certain value. Before adding checks for
     * duplicates.
     *
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
                //if availability facet reduce tag to most restrictive
                if (name.equals(FacetConstants.FIELD_AVAILABILITY)) {
                    reduceAvailability(value);
                } else {
                    doc.addField(name, value);
                }
            } //ignore double values don't add them
        }
    }
    
    public Collection<Object> getDocField(String name) {
        if (doc == null) {
            return null;
        } else {
            return doc.getFieldValues(name);
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
     * Return the list of landing page resources.
     *
     * @return the list
     */
    public List<Resource> getLandingPageResources() {
        return landingPageResources;
    }

    /**
     * Return the list of search page resources.
     *
     * @return the list
     */
    public List<Resource> getSearchPageResources() {
        return searchPageResources;
    }

    /**
     * Add a meta data resource to the list of resources of that type.
     *
     * Whenever the type is not one of a type supported by the CMDI
     * specification, a warning is logged.
     *
     * @param resource meta data resource
     * @param type type of the resource
     * @param mimeType mime type associated with the resource
     */
    public void addResource(String resource, String type, String mimeType) {
        if (METADATA_TYPE.equals(type)) {
            metaDataResources.add(new Resource(resource, type, mimeType));
        } else if (DATA_RESOURCE_TYPE.equals(type)) {
            dataResources.add(new Resource(resource, type, mimeType));
        } else if (SEARCH_SERVICE_TYPE.equals(type)) {
            searchResources.add(new Resource(resource, type, mimeType));
        } else if (LANDING_PAGE_TYPE.equals(type)) {
            // omit multiple LandingPages
            if(landingPageResources.isEmpty())
                landingPageResources.add(new Resource(resource, type, mimeType));
            else
                LOG.warn("Ignoring surplus landingpage: {}", resource);
        } else if (SEARCH_PAGE_TYPE.equals(type)) {
            searchPageResources.add(new Resource(resource, type, mimeType));
        } else {
            LOG.warn("Ignoring unsupported resource type "
                    + type + ", name=" + resource);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /*
     * In case that Availability facet has more then one value use the most restrictive tag from PUB, ACA and RES 
     */
    private void reduceAvailability(String value) {
        Collection<Object> currentValues = doc.getFieldValues(FacetConstants.FIELD_AVAILABILITY);

        //the first value
        if (currentValues == null) {
            doc.addField(FacetConstants.FIELD_AVAILABILITY, value);
            return;
        }

        int lvlNew = availabilityToLvl(value);
        if (lvlNew == -1) { //other tags, add them, uniqueness has already being checked
            doc.addField(FacetConstants.FIELD_AVAILABILITY, value);
            return;
        }

        //current level of availability
        int lvlCur = -1;
        for (Object val : currentValues) {
            int rhs = availabilityToLvl(val.toString());
            if (lvlCur < rhs) {
                lvlCur = rhs;
            }
        }

        //if new values is more restrictive replace the old with new
        if (lvlNew > lvlCur) {
            SolrInputField fOld = doc.get(FacetConstants.FIELD_AVAILABILITY);
            SolrInputField fNew = new SolrInputField(FacetConstants.FIELD_AVAILABILITY);

            fNew.addValue(value, 1.0f); //new, more restrictive value
            for (Object val : fOld.getValues()) { //copy other tags
                if (availabilityToLvl(val.toString()) == -1) {
                    fNew.addValue(val, 1.0f);
                }
            }

            doc.replace(FacetConstants.FIELD_AVAILABILITY, fOld, fNew);
        }

    }

    private int availabilityToLvl(String availabilty) {
        if (availabilty == null) {
            return 0;
        }

        switch (availabilty) {
            case FacetConstants.AVAILABILITY_LEVEL_PUB:
                return 1;
            case FacetConstants.AVAILABILITY_LEVEL_ACA:
                return 2;
            case FacetConstants.AVAILABILITY_LEVEL_RES:
                return 3;
            default:
                return -1; // other tags
        }
    }
}
