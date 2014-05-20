package eu.clarin.cmdi.vlo.importer;

public class ResourceClassPostProcessor extends LanguageCodePostProcessor {
    /**
     * Postprocess ResourceClass values
     * @param value extracted ResourcClass information
     * @return Value with upper case first letter
     */
    @Override
    public String process(String value) {
        String result =value;
        if(result.length() > 1) {
            result = result.substring(0, 1).toUpperCase().concat(result.substring(1, result.length()));
        }
        return result.toString();
    }
}
