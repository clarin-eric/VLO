package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.List;
import java.util.Map;

public interface ValueMappingFactory {
    public Map<String, ConditionTargetSet> getValueMappings(String fileName, FacetConceptMapping facetConceptMapping);

}