package eu.clarin.cmdi.vlo.importer;

import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.util.Collections;

/**
 * Represents a document of CMDI data.
 */
public class CMDIDataSolrImpl extends CMDIDataBaseImpl<SolrInputDocument> {

    /**
     * The associated solr document (not send to the solr server yet)
     */
    private SolrInputDocument doc;

    public CMDIDataSolrImpl(FieldNameService fieldNameService) {
        super(fieldNameService);
    }

    @Override
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
    @Override
    public void addDocField(ValueSet valueSet, boolean caseInsensitive) {
        final String fieldName = valueSet.getTargetFacetName();
        final String value = valueSet.getValue();
        if (fieldNameService.getFieldName(FieldKey.ID).equals(fieldName)) {
            setId(value.trim());
        } else {
            addDocField(fieldName, value, caseInsensitive);
        }
    }

    @Override
    public void replaceDocField(ValueSet valueSet, boolean caseInsensitive) {
        if (valueSet.getTargetFacetName() == null) {
            LOG.error("Cannot replace field wihtout target facet name - valueSet={}, caseInsensitive={}", valueSet);
            throw new NullPointerException("TargetFacetName not set in valueSet");
        } else {
            replaceDocField(valueSet.getTargetFacetName(), valueSet.getValue(), caseInsensitive);
        }
    }

    @Override
    public void addDocField(String fieldName, Object value, boolean caseInsensitive) {
        if (fieldName == null) {
            LOG.error("Cannot add field without fieldName - fieldName='{}', value='{}', caseInsensitive={}", fieldName, value, caseInsensitive);
            throw new NullPointerException("Field name not set");
        } else {
            handleDocField(fieldName, value, caseInsensitive);
        }
    }

    @Override
    public void replaceDocField(String name, Object value, boolean caseInsensitive) {
        if (name == null) {
            LOG.error("Cannot replace field without fieldName - name='{}', value='{}', caseInsensitive={}", name, value, caseInsensitive);
            throw new NullPointerException("Field name not set");
        } else {
            if (this.doc != null) {
                this.doc.removeField(name);
            }
            this.addDocField(name, value, caseInsensitive);
        }
    }

    @Override
    public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {
        if (this.getDocField(valueSet.getTargetFacetName()) == null) {
            this.addDocField(valueSet, caseInsensitive);
        }
    }

    @Override
    public void removeField(String name) {
        if (name == null) {
            LOG.error("Cannot remove field without name");
        } else {
            this.doc.removeField(name);
        }
    }

    @Override
    public boolean hasField(String name) {
        return this.doc.containsKey(name);
    }

    @Override
    public Collection<Object> getFieldValues(String name) {
        if (name == null) {
            LOG.error("Cannot get values for field without name");
            return Collections.emptySet();
        } else {
            return this.doc.getFieldValues(name);
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
    private void handleDocField(String name, Object value, boolean caseInsensitive) {
        if (name == null) {
            LOG.error("Cannot set field value without fieldName - name='{}', value='{}', caseInsensitive={}", name, value, caseInsensitive);
            throw new NullPointerException("Field name not set");
        } else {
            if (doc == null) {
                doc = new SolrInputDocument();
            }
            if (value != null) {
                if (value instanceof String) {
                    if (((String) value).trim().isEmpty()) {
                        return;
                    } else if (caseInsensitive) {
                        value = ((String) value).toLowerCase();
                    }
                }
                Collection<Object> fieldValues = doc.getFieldValues(name);
                if (fieldValues == null || !fieldValues.contains(value)) {
                    doc.addField(name, value);
                } // ignore double values don't add them
            }
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

}
