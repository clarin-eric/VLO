package eu.clarin.cmdi.vlo.pages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import eu.clarin.cmdi.vlo.FacetConstants;

public class DocumentAttributeList implements Iterator<DocumentAttribute> {

    private final Map<String, Collection<Object>> fieldValueMap;
    private Iterator<String> iterator;
    private Comparator<String> attributesComparator = new AttributesComparator(Arrays.asList(FacetConstants.FIELD_NAME,
            FacetConstants.FIELD_DESCRIPTION, FacetConstants.FIELD_LANGUAGE, FacetConstants.FIELD_COUNTRY, FacetConstants.FIELD_CONTINENT,
            FacetConstants.FIELD_YEAR, FacetConstants.FIELD_ID));

    public DocumentAttributeList(Map<String, Collection<Object>> map) {
        this.fieldValueMap = map;
        SortedSet<String> sorted = new TreeSet<String>(attributesComparator);
        sorted.addAll(map.keySet());
        this.iterator = sorted.iterator();
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
