package eu.clarin.cmdi.vlo.importer;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

/**
 * Represents a document of CMDI data.
 */
public class CMDIData extends CMDIDataBaseImpl<SolrInputDocument> {

    /**
     * The associated solr document (not send to the solr server yet)
     */
    private SolrInputDocument doc;

    public CMDIData(FieldNameService fieldNameService) {
        super(fieldNameService);
    }

    public SolrInputDocument getDocument() {
        return doc;
    }

    /**
     * Sets a field in the doc to a certain value. Well, at least calls another
     * (private) method that actually does this.
     *
     * @param valueSet
     * @param caseInsensitive
     */
    public void addDocField(ValueSet valueSet, boolean caseInsensitive) {
        addDocField(valueSet.getTargetFacetName(), valueSet.getValue(), caseInsensitive);
    }

    public void replaceDocField(ValueSet valueSet, boolean caseInsensitive) {
        replaceDocField(valueSet.getTargetFacetName(), valueSet.getValue(), caseInsensitive);
    }

    public void addDocField(String fieldName, String value, boolean caseInsensitive) {
        if (fieldNameService.getFieldName(FieldKey.ID).equals(fieldName)) {
            setId(value.trim());
        } else {
            handleDocField(fieldName, value, caseInsensitive);
        }
    }

    public void replaceDocField(String name, String value, boolean caseInsensitive) {
        if (this.doc != null) {
            this.doc.removeField(name);
        }
        this.addDocField(name, value, caseInsensitive);
    }

    public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {
        if (this.getDocField(valueSet.getTargetFacetName()) == null) {
            this.addDocField(valueSet, caseInsensitive);
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
                // if availability facet reduce tag to most restrictive
                if (name.equals(fieldNameService.getFieldName(FieldKey.AVAILABILITY))
                        || name.equals(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE))) {
                    reduceAvailability(name, value);
                } else {
                    doc.addField(name, value);
                }
            } // ignore double values don't add them
        }
    }

    @Override
    public Collection<Object> getDocField(String name) {
        if (doc == null) {
            return null;
        } else {
            return doc.getFieldValues(name);
        }
    }

    /**
     * In case that Availability facet has more then one value use the most
     * restrictive tag from PUB, ACA and RES. TODO: Move this to post processor
     *
     * @param field field to reduce availability values in
     * @param value value to insert (add or replace)
     */
    protected void reduceAvailability(String field, String value) {
        Collection<Object> currentValues = doc.getFieldValues(field);
        // the first value
        if (currentValues == null) {
            doc.addField(field, value);
            return;
        }
        int lvlNew = availabilityToLvl(value);
        if (lvlNew == -1) {
            // other tags, add them, uniqueness has already being checked
            doc.addField(field, value);
            return;
        }
        // current level of availability
        int lvlCur = -1;
        for (Object val : currentValues) {
            int rhs = availabilityToLvl(val.toString());
            if (lvlCur < rhs) {
                lvlCur = rhs;
            }
        }
        // if new values is more restrictive replace the old with new
        if (lvlNew > lvlCur) {
            SolrInputField fOld = doc.get(field);
            SolrInputField fNew = new SolrInputField(field);
            fNew.addValue(value); // new, more restrictive value
            for (Object val : fOld.getValues()) {
                // copy other tags
                if (availabilityToLvl(val.toString()) == -1) {
                    fNew.addValue(val);
                }
            }
            doc.replace(field, fOld, fNew);
        }
    }

}
