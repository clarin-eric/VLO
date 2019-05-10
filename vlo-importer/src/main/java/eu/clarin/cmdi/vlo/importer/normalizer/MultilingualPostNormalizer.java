package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Post processor that normalizes metadata values indicating the number of
 * languages that are included in or described/supported by the resource
 */
public class MultilingualPostNormalizer extends AbstractPostNormalizer {
    public final static String VALUE_MULTILINGUAL = "yes";
    public final static String VALUE_NOT_MULTILINGUAL = "no";
    
    private final String monolingualIndicatorValue = "monolingual";
    private final Set<String> multilingualityIndicatorValues = new HashSet<>();    
    {
        multilingualityIndicatorValues.add("bilingual");
        multilingualityIndicatorValues.add("multilingual");
    }
    
    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        // in case the number of languages is provided as integer
        try {
            int numberCount = Integer.parseInt(value);
            if(numberCount > 1)
                return Collections.singletonList(VALUE_MULTILINGUAL);
            else
                return Collections.singletonList(VALUE_NOT_MULTILINGUAL);
        } catch(NumberFormatException nfe) {
            ; // no Integer
        }
        
        // in case where vocabulary of component 'cmdi-multilinguality' is used
        if(multilingualityIndicatorValues.contains(value.toLowerCase()))
            return Collections.singletonList(VALUE_MULTILINGUAL);
        else if(value.toLowerCase().equals(monolingualIndicatorValue))
            return Collections.singletonList(VALUE_NOT_MULTILINGUAL);
        else
            // every other value -> ignored
            return Collections.singletonList(null);
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
