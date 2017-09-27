package eu.clarin.cmdi.vlo.importer.processor;

import com.google.common.collect.ImmutableMap;

import eu.clarin.cmdi.vlo.importer.CMDIData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ContinentNamePostProcessor implements PostProcessor {

    private final static Map<String, String> CONTINENT_CODE_MAP
            = ImmutableMap.<String, String>builder()
                    .put("AF", "Africa")
                    .put("AS", "Asia")
                    .put("EU", "Europe")
                    .put("NA", "North America")
                    .put("SA", "South America")
                    .put("OC", "Oceania")
                    .put("AN", "Antarctica")
                    .build();

    /**
     * Replaces two-letter continent codes with continent names
     */
    @Override
    public List<String> process(final String value, CMDIData cmdiData) {
        if (value == null) {
            return Collections.singletonList(null);
        } else {
            final String normalized = CONTINENT_CODE_MAP.getOrDefault(value, value);
            return Collections.singletonList(normalized);
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
