package eu.clarin.cmdi.vlo.pages;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrDocument;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.FacetConstants;

public class DocumentAttributesDataProvider extends SortableDataProvider<DocumentAttribute> {
    
    private static final Set<String> IGNORE_FACETS = new HashSet<String>();
    static {
        IGNORE_FACETS.add(FacetConstants.FIELD_RESOURCE_TYPE);
    }

    private static final long serialVersionUID = 1L;
    
    private transient DocumentAttributeList attributeList;

    public DocumentAttributesDataProvider(SolrDocument solrDocument) {
        if (solrDocument != null) {
            Map<String, Collection<Object>> fieldMap = new HashMap<String, Collection<Object>>();
            Map<String, Collection<Object>> fieldValuesMap = solrDocument.getFieldValuesMap();
            for (String entry : fieldValuesMap.keySet()) {
                if (!ignoreEntry(entry)) { //Filter out all '_' starting (internal) fields
                    fieldMap.put(entry, fieldValuesMap.get(entry));
                }
            }
            attributeList = new DocumentAttributeList(fieldMap);
        } else {
            attributeList = new DocumentAttributeList(Collections.singletonMap("Document not found", (Collection<Object>) null));
        }
    }
    
    private boolean ignoreEntry(String entry) {
//        if(entry.equals(FacetConstants.FIELD_COMPLETE_METADATA)){
//            return false; // Do not ignore the "complete metadata" entry, even though it starts with a _
//        }
        return entry.startsWith("_") || IGNORE_FACETS.contains(entry);
    }

    @Override
    public Iterator<? extends DocumentAttribute> iterator(int first, int count) {
        return attributeList;
    }

    @Override
    public IModel<DocumentAttribute> model(DocumentAttribute object) {
        return new Model<DocumentAttribute>(object);
    }

    @Override
    public int size() {
        return attributeList.size();
    }

}
