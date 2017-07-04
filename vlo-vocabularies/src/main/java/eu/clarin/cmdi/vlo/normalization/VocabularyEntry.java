package eu.clarin.cmdi.vlo.normalization;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;

import eu.clarin.cmdi.vlo.pojo.CrossMapping;

public class VocabularyEntry {

    private final String originalVal;
    private final String normalizedValue;
    private final boolean isRegEx;
    private final Map<String, String> crossMap;

    public VocabularyEntry(String originalVal, String normalizedValues, boolean isRegEx, List<CrossMapping> crossMap) {
        this.originalVal = originalVal;
        this.normalizedValue = normalizedValues;
        this.isRegEx = isRegEx;

        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        if (crossMap != null) {
            crossMap.forEach((cm) -> {
                mapBuilder.put(cm.getFacet(), cm.getValue());
            });
        }
        this.crossMap = mapBuilder.build();
    }

    public String getOriginalVal() {
        return originalVal;
    }

    public String getNormalizedValue() {
        return normalizedValue;
    }

    public boolean isRegEx() {
        return isRegEx;
    }

    public Map<String, String> getCrossMap() {
        return crossMap;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s, isRegEx=%s, %s", originalVal, normalizedValue, isRegEx, crossMap);
    }

}
