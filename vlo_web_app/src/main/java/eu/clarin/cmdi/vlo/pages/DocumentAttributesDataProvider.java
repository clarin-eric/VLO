package eu.clarin.cmdi.vlo.pages;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.Arrays;
import java.util.HashSet;

public class DocumentAttributesDataProvider extends SortableDataProvider<DocumentAttribute, String> {

    private static final Map<String, Collection<Object>> NOT_FOUND_MAP = Collections.singletonMap("Document not found", (Collection<Object>) null);
    private static final Collection<String> IGNORE_FACETS = new HashSet<String>(Arrays.asList(FacetConstants.FIELD_FORMAT));

    private static final long serialVersionUID = 1L;

    private final Map<String, Collection<Object>> fieldMap;

    public DocumentAttributesDataProvider(SolrDocument solrDocument) {
        if (solrDocument != null) {
            fieldMap = new HashMap<String, Collection<Object>>();
            Map<String, Collection<Object>> fieldValuesMap = solrDocument.getFieldValuesMap();
            for (String entry : fieldValuesMap.keySet()) {
                if (!ignoreEntry(entry)) { //Filter out all '_' starting (internal) fields
                    fieldMap.put(entry, fieldValuesMap.get(entry));
                }
            }

        } else {
            fieldMap = null;
        }
    }

    private boolean ignoreEntry(String entry) {
//        if(entry.equals(FacetConstants.FIELD_COMPLETE_METADATA)){
//            return false; // Do not ignore the "complete metadata" entry, even though it starts with a _
//        }
        return entry.startsWith("_") || IGNORE_FACETS.contains(entry);
    }

    @Override
    public Iterator<? extends DocumentAttribute> iterator(long first, long count) {
        if (fieldMap != null) {
            return new DocumentAttributeList(fieldMap);
        } else {
            return new DocumentAttributeList(NOT_FOUND_MAP);
        }
    }

    @Override
    public IModel<DocumentAttribute> model(DocumentAttribute object) {
        return new Model<DocumentAttribute>(object);
    }

    @Override
    public long size() {
        if (fieldMap != null) {
            return fieldMap.size();
        } else {
            return NOT_FOUND_MAP.size();
        }
    }

}
