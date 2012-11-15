package eu.clarin.cmdi.vlo.importer;

import java.util.Map;

import eu.clarin.cmdi.vlo.FacetConstants;

public class LanguageLinkPostProcessor extends LanguageCodePostProcessor {
	private static LanguageCodePostProcessor languageCodePostProcessor = new LanguageCodePostProcessor();
	
    /**
     * Returns the link to language information
     * If no mapping is found the original value is returned.
     */
    @Override
    public String process(String value) {        
		String result = languageCodePostProcessor.process(value);
        if (value != null) {
            String langCode = extractISO639LanguageCode(value);
            if(langCode.length() == 3)
            	result = "<a href=\""+FacetConstants.LANGUAGE_LINK_PREFIX + langCode+"\">"+getLanguageNameForLanguageCode(langCode.toUpperCase())+"</a>";
        }
        return result;
    }
    
    /**
     * Try to guess the ISO 639-3 language code from value
     * @param value
     * @return ISO 639-3 code, or parameter value if it could not determined
     */
    protected String extractISO639LanguageCode(String value) {
        String result = value;
        if (value.length() != 2 && value.length() != 3) {
            if (value.startsWith(ISO639_3_PREFIX)) {
                result = value.substring(ISO639_3_PREFIX.length());
            } else if (value.startsWith(SIL_CODE_PREFIX) || value.startsWith(SIL_CODE_PREFIX_alt)) {
                result = value.substring(value.lastIndexOf("-")+1);
                Map<String, String> silToISOMap = getSilToIso639Map();
                String isoCode = silToISOMap.get(result.toUpperCase());
                if (isoCode != null) {
                    result = isoCode;
                }
            } else {				// guessing based on language name
            	if(getLanguageNameToIso639Map().containsKey(value))
            		result = getLanguageNameToIso639Map().get(value).toLowerCase();
            }
        }

        // SIL code?
        if(result.length() == 2) {
        	Map<String, String> silToISOMap = getSilToIso639Map();
            String isoCode = silToISOMap.get(result.toUpperCase());
            if (isoCode != null) {
                result = isoCode;
            }
        }
        
        return result;
    }
}
