package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LanguageNamePostProcessor extends LanguageCodePostProcessor {

    private final static Logger LOG = LoggerFactory.getLogger(LanguageNamePostProcessor.class);

    private Map<String, String> twoLetterCodesMap;
    private Map<String, String> threeLetterCodesMap;

    /**
     * Returns the language name based on the mapping defined in the CMDI components: See http://trac.clarin.eu/ticket/40 for the mapping.
     * If no mapping is found the original value is returned.
     */
    @Override
    public List<String> process(String value) {
        final LanguageCodeUtils languageCodeUtils = MetadataImporter.languageCodeUtils;
        
        String result = value;
        if (value != null) {
            String langCode = extractLanguageCode(value);
            if(langCode.startsWith(CODE_PREFIX))
                langCode = langCode.substring(CODE_PREFIX.length());
            
            if (langCode.length() == 2) {
                twoLetterCodesMap = languageCodeUtils.getTwoLetterCountryCodeMap();
                String name = twoLetterCodesMap.get(langCode.toUpperCase());
                if (name != null) {
                    result = name;
                }
            } else if (langCode.length() == 3) {
                threeLetterCodesMap = languageCodeUtils.getThreeLetterCountryCodeMap();
                String name = threeLetterCodesMap.get(langCode.toUpperCase());
                if (name != null) {
                    result = name;
                }
            }
        }
        List<String> resultList = new ArrayList<String>();
        resultList.add(result);
        return resultList;
    }
}
