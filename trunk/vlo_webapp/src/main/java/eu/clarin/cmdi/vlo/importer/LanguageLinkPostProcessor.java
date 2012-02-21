package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;


public class LanguageLinkPostProcessor extends LanguageCodePostProcessor {

    /**
     * Returns the link to language information
     * If no mapping is found the original value is returned.
     */
    @Override
    public String process(String value) {
        String result = value;
        if (value != null) {
            String langCode = extractLanguageCode(value);
            if(langCode.length() == 3)
            	result = "<a href=\""+FacetConstants.LANGUAGE_LINK_PREFIX + langCode+"\">"+getLanguageNameForLanguageCode(langCode.toUpperCase())+"</a>";
        }
        return result;
    }
}
