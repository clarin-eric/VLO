package eu.clarin.cmdi.vlo.importer.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import org.apache.commons.lang.WordUtils;

public class LanguageCodePostNormalizer extends AbstractPostNormalizerWithVocabularyMap {

    protected static final String CODE_PREFIX = "code:";
    protected static final String LANG_NAME_PREFIX = "name:";
    protected static final String ISO639_2_PREFIX = "ISO639-2:";
    protected static final String ISO639_3_PREFIX = "ISO639-3:";
    protected static final String SIL_CODE_PREFIX = "RFC1766:x-sil-";
    protected static final String SIL_CODE_PREFIX_alt = "RFC-1766:x-sil-";

    private static final Pattern RFC1766_Pattern = Pattern.compile("^([a-z]{2,3})[-_][a-zA-Z]{2}$");
    private final LanguageCodeUtils languageCodeUtils;

    public LanguageCodePostNormalizer(VloConfig config, LanguageCodeUtils languageCodeUtils) {
        super(config);
        this.languageCodeUtils = languageCodeUtils;
    }

    /**
     * Returns the language code based on the mapping defined in the CMDI
     * components: See http://trac.clarin.eu/ticket/40 for the mapping. If no
     * mapping is found the original value is returned.
     *
     * @param value extracted language value (language code or language name)
     * from CMDI file
     * @return ISO 639-3 code
     */
    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {
        List<String> resultList = new ArrayList<String>();

        if (value != null) {
            resultList.add(extractLanguageCode(value));
        } else {
            resultList.add(null);
        }
        return resultList;
    }

    @Override
    public String getNormalizationMapURL() {
        return getConfig().getLanguageNameVariantsUrl();
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

    protected String extractLanguageCode(String value) {
        String result = value;

        result = result.replaceFirst(ISO639_2_PREFIX, "").replaceFirst(ISO639_3_PREFIX, "").replaceFirst(SIL_CODE_PREFIX, "").replaceFirst(SIL_CODE_PREFIX_alt, "");

        // map known language name variants to their offical name
        result = normalize(result, result);

        // input is already ISO 639-3?
        if (languageCodeUtils.getIso639ToLanguageNameMap().keySet().contains(result.toUpperCase())) {
            return CODE_PREFIX + result.toLowerCase();
        }
        // input is 2-letter code -> map to ISO 639-3
        if (languageCodeUtils.getSilToIso639Map().containsKey(result.toLowerCase())) {
            return CODE_PREFIX + languageCodeUtils.getSilToIso639Map().get(result.toLowerCase());
        }

        if (languageCodeUtils.getLanguageNameToIso639Map().containsKey(WordUtils.capitalize(result))) { // (english) language name?
            return CODE_PREFIX + languageCodeUtils.getLanguageNameToIso639Map().get(WordUtils.capitalize(result));
        }

        // convert ISO 639-2/T codes to ISO 639-3
        if (languageCodeUtils.getIso6392TToISO6393Map().containsKey(result.toLowerCase())) {
            return CODE_PREFIX + languageCodeUtils.getIso6392TToISO6393Map().get(result.toLowerCase());
        }

        Matcher matcher = RFC1766_Pattern.matcher(result);
        if (matcher.find()) {
            return extractLanguageCode(matcher.group(1));
        }

        // language code not identified? -> language name
        if (!result.equals("")) {
            result = LANG_NAME_PREFIX + WordUtils.capitalize(result);
        }
        return result;
    }

}
