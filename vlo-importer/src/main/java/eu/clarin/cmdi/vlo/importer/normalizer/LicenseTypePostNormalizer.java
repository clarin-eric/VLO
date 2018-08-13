package eu.clarin.cmdi.vlo.importer.normalizer;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.DocFieldContainer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Vocabulary map based post processing with a fallback that transfers values
 * from the availability facet.
 * 
 * @see <a href="https://github.com/clarin-eric/VLO/issues/39">#39</a>
 * @see <a href="https://github.com/clarin-eric/VLO/issues/55">#55</a>
 * @author Twan Goosen
 */
public class LicenseTypePostNormalizer extends AbstractPostNormalizerWithVocabularyMap {
    private FieldNameService fieldNameService;

    public LicenseTypePostNormalizer(VloConfig config) {
        super(config);
        this.fieldNameService = new FieldNameServiceImpl(config);
    }

    @Override
    public List<String> process(final String value, DocFieldContainer cmdiData) {
        if (value != null) {
            final String normalizedVal = normalize(value);
            //Availability variants can be normalized with multiple values, in vocabulary they are separated with ;
            if (normalizedVal != null) {
                return Arrays.asList(normalizedVal.split(";"));
            }
        }
        //no (normalized) value - get from availability facet
        return transferValuesFromAvailability(cmdiData);
    }

    /**
     * Transfers license type values from the availability facet - meant as a fallback
     * @param cmdiData
     * @return 
     */
    private List<String> transferValuesFromAvailability(DocFieldContainer cmdiData) {
        if (cmdiData != null) {
            final Collection<Object> availabilityValues = cmdiData.getDocField(fieldNameService.getFieldName(FieldKey.AVAILABILITY));
            if (availabilityValues != null) {
                //turn into string list
                final List<String> values = Lists.newArrayList(Collections2.transform(availabilityValues, new Function<Object, String>() {
                    @Override
                    public String apply(Object t) {
                        return t.toString();
                    }
                }));
                //only transfer 'valid' license type values (pub, aca, res)
                values.retainAll(FacetConstants.LICENSE_TYPE_VALUES);
                return values;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String getNormalizationMapURL() {
        return getConfig().getLicenseTypeMapUrl();
    }

    @Override
    public boolean doesProcessNoValue() {
        return true;
    }
}
