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

public class DocumentAttributesDataProvider extends SortableDataProvider<DocumentAttribute> {

    private static final long serialVersionUID = 1L;

    private transient DocumentAttributeList attributeList;

    public DocumentAttributesDataProvider(SolrDocument solrDocument) {
        if (solrDocument != null) {
            Map<String, Collection<Object>> fieldMap = new HashMap<String, Collection<Object>>();
            Map<String, Collection<Object>> fieldValuesMap = solrDocument.getFieldValuesMap();
            for (String entry : fieldValuesMap.keySet()) {
                if (!entry.startsWith("_")) { //Filter out all '_' starting (internal) fields
                    fieldMap.put(entry, fieldValuesMap.get(entry));
                }
            }
            attributeList = new DocumentAttributeList(fieldMap);
        } else {
            attributeList = new DocumentAttributeList(Collections.singletonMap("Document not found", (Collection<Object>) null));
        }
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
