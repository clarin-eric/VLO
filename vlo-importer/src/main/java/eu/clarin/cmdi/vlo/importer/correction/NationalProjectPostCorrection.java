package eu.clarin.cmdi.vlo.importer.correction;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;

import java.util.Arrays;
import java.util.List;


/**
 * Adds information about the affiliation of a metadata file to a national
 * project (like CLARIN-X etc.) into facet nationalProject
 *
 * @author Thomas Eckart
 *
 */
public class NationalProjectPostCorrection extends AbstractPostCorrectionWithVocabularyMap {


    public NationalProjectPostCorrection(VloConfig config) {
        super(config);
    }

    /**
     * Returns the national project based on the mapping in
     * Configuration.getNationalProjectMapUrl() If no mapping was found empty
     * String is returned
     *
     * @return
     */
    @Override
    public List<String> process(String value, CMDIData cmdiData) {
        return Arrays.asList(normalize(value.trim(), ""));
    }

    @Override
    public String getNormalizationMapURL() {
        return getConfig().getNationalProjectMapping();
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

}
