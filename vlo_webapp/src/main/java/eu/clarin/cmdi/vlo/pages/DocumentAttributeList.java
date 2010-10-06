package eu.clarin.cmdi.vlo.pages;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class DocumentAttributeList implements Iterator<DocumentAttribute> {

    private final Map<String, Collection<Object>> fieldValueMap;
    Iterator<String> iterator;

    public DocumentAttributeList(Map<String, Collection<Object>> map) {
        this.fieldValueMap = map;
        this.iterator = map.keySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public DocumentAttribute next() {
        String key = iterator.next();
        return new DocumentAttribute(key, fieldValueMap.get(key));
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    public int size() {
        return fieldValueMap.size();
    }

}
