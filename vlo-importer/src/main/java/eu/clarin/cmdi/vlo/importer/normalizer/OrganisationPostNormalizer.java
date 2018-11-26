package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import java.util.Arrays;
import java.util.List;

public class OrganisationPostNormalizer extends AbstractPostNormalizerWithVocabularyMap {

    public OrganisationPostNormalizer(VloConfig config) {
        super(config);
    }

    /**
     * Splits values for organisation facet at delimiter ';' and replaces
     * organisation name variants with their official name from a controlled
     * vocabulary
     *
     * @param value extracted organisation name/names
     * @return List of organisation names (splitted at semicolon) and variations
     * replaced with controlled vocabulary
     */
    @Override
    public List<String> process(String value, DocFieldContainer cmdiData) {

        String[] splitArray = normalizeInputString(value).split(";");
        for (int i = 0; i < splitArray.length; i++) {
            String orgaName = splitArray[i];
            splitArray[i] = normalize(normalizeVariant(orgaName), splitArray[i]);
        }

        return Arrays.asList(splitArray);

    }

    @Override
    public String getNormalizationMapURL() {
        return getConfig().getOrganisationNamesUrl();
    }

    private String normalizeInputString(String value) {
        return value.replaceAll("\\s+", " ");
    }

    private String normalizeVariant(String key) {
        return key.toLowerCase().replaceAll("-", " ");
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
