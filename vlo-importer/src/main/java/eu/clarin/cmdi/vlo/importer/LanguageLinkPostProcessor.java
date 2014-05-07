package eu.clarin.cmdi.vlo.importer;

public class LanguageLinkPostProcessor extends LanguageCodePostProcessor {
    /**
     * Returns the link to language information
     * If no mapping is found the original value is returned.
     * @param value extracted language information
     * @return HTML link to the CLARIN language information page
     */
    @Override
    public String process(String value) {
	String langCode = super.process(value);
        String result = langCode;
        if (langCode != null) {
            if(langCode.length() == 3)
            	result = "<a href=\""+ MetadataImporter.config.getLanguageLinkPrefix() + langCode+"\">"+getLanguageNameForLanguageCode(langCode.toUpperCase())+"</a>";
        }
        return result;
    }
}
