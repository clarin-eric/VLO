package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;

public class LanguageLinkPostProcessor extends LanguageCodePostProcessor {
    /**
     * Returns the link to language information
     * If no mapping is found the original value is returned.
     * @param value extracted language information
     * @return HTML link to the CLARIN language information page
     */
    @Override
    public List<String> process(String value) {
	String langCode = super.process(value).get(0);
        String result = langCode;
        if (langCode != null) {
            if(langCode.startsWith(CODE_PREFIX)) {
                langCode = langCode.substring(CODE_PREFIX.length());
            	result = "<a href=\""+ MetadataImporter.config.getLanguageLinkPrefix() + langCode+"\">"+getLanguageNameForLanguageCode(langCode.toUpperCase())+"</a>";
            } else if(langCode.startsWith(LANG_NAME_PREFIX)) {
                result = langCode.substring(LANG_NAME_PREFIX.length());
            }
        }
        List<String> resultList = new ArrayList<String>();
        resultList.add(result);
        return resultList;
    }
}
