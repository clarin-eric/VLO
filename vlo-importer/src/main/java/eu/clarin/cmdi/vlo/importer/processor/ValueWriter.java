/*
 * Copyright (C) 2018 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.importer.processor;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import static eu.clarin.cmdi.vlo.importer.processor.LanguageDefaults.DEFAULT_LANGUAGE;
import static eu.clarin.cmdi.vlo.importer.processor.LanguageDefaults.ENGLISH_LANGUAGE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ValueWriter {

    private final static Logger LOG = LoggerFactory.getLogger(ValueWriter.class);

    private final FieldNameServiceImpl fieldNameService;
    private final Map<String, AbstractPostNormalizer> postProcessors;

    public ValueWriter(VloConfig config, Map<String, AbstractPostNormalizer> postProcessors) {
        this.postProcessors = postProcessors;
        this.fieldNameService = new FieldNameServiceImpl(config);
    }

    /**
     * @param cmdiData cmdiData representation of the CMDI document
     * @param facetValuesMap A map of FacetConfigurations (key)/Lists of
     * ValueSets (value)
     */
    public void writeValuesToDoc(CMDIData cmdiData, Map<FacetConfiguration, List<ValueSet>> facetValuesMap) {
        for (Map.Entry<FacetConfiguration, List<ValueSet>> entry : facetValuesMap.entrySet()) {

            for (ValueSet valueSet : entry.getValue()) {
                insertFacetValue(cmdiData, entry.getKey(), valueSet.getValueLanguagePair());
                if (!(entry.getKey().getAllowMultipleValues() || entry.getKey().getMultilingual())) {
                    break;
                }
            }

        }
    }

    /**
     * Sets default value if Null and if defined for the facet
     *
     * @param facetValuesMap processed facet configurations
     * @param cmdiData current CMDI data object
     */
    public void writeDefaultValues(CMDIData cmdiData, Map<FacetConfiguration, List<ValueSet>> facetValuesMap) {
        Collection<FacetConfiguration> facetList = facetValuesMap.keySet();
        for (FacetConfiguration facetConfig : facetList) {
            if (cmdiData.getDocField(facetConfig.getName()) == null && this.postProcessors.containsKey(facetConfig.getName()) && this.postProcessors.get(facetConfig.getName()).doesProcessNoValue()) {
                final ArrayList<Pair<String, String>> valueLangPairList = new ArrayList<>();
                addValuesToList(facetConfig.getName(), this.postProcessors.get(facetConfig.getName()).process(null, cmdiData), valueLangPairList, DEFAULT_LANGUAGE);
                insertFacetValues(facetConfig, valueLangPairList, cmdiData, false);
            }
        }
    }

    /**
     * @param cmdiData cmdiData representation of the CMDI document
     * @param facetConfig FacetConfiguration of the target facet
     * @param valueLangPair Value/language Pair
     */
    private void insertFacetValue(CMDIData cmdiData, FacetConfiguration facetConfig, Pair<String, String> valueLangPair) {

        String fieldValue = valueLangPair.getLeft().trim();

        if (facetConfig.getName().equals(fieldNameService.getFieldName(FieldKey.DESCRIPTION))) {
            fieldValue = "{" + valueLangPair.getRight() + "}" + fieldValue;
        }

        cmdiData.addDocField(facetConfig.getName(), fieldValue, facetConfig.isCaseInsensitive());
    }

    /**
     * Inserts values to the representation of the CMDI document
     *
     * @param facetConfig facet configuration
     * @param valueLangPairList
     * @param cmdiData representation of the CMDI document
     * @param overrideExistingValues should existing values be overridden (=
     * delete + insert)?
     */
    private void insertFacetValues(FacetConfiguration facetConfig, List<Pair<String, String>> valueLangPairList, CMDIData cmdiData, boolean overrideExistingValues) {

        for (int i = 0; i < valueLangPairList.size(); i++) {
//            if (!allowMultipleValues && i > 0) {
//                break;
//            }
            String fieldValue = valueLangPairList.get(i).getLeft().trim();
            if (facetConfig.getName().equals(fieldNameService.getFieldName(FieldKey.DESCRIPTION))) {
                fieldValue = "{" + valueLangPairList.get(i).getRight() + "}" + fieldValue;
            }
            if (overrideExistingValues) {
                if (cmdiData.getDocField(facetConfig.getName()) != null) {
                    LOG.info("overriding existing value(s) in facet {} with value" + facetConfig.getName(), fieldValue);
                }
                cmdiData.replaceDocField(facetConfig.getName(), fieldValue, facetConfig.isCaseInsensitive());

                if (!(facetConfig.getAllowMultipleValues() || facetConfig.getMultilingual())) {
                    break;
                }
            } else {
                if (!(facetConfig.getAllowMultipleValues() || facetConfig.getMultilingual()) && cmdiData.getDocField(facetConfig.getName()) != null) {
                    LOG.info("value for facet {} is set already. Since multiple value are not allowed value {} will be ignored!", facetConfig.getName(), fieldValue);
                    break;
                }
                cmdiData.addDocField(facetConfig.getName(), fieldValue, facetConfig.isCaseInsensitive());
            }
        }
    }

    private void addValuesToList(String facetName, final List<String> values, List<Pair<String, String>> valueLangPairList, final String languageCode) {
        for (String value : values) {
            // ignore non-English language names for facet LANGUAGE_CODE
            if (facetName.equals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)) && !languageCode.equals(ENGLISH_LANGUAGE) && !languageCode.equals(DEFAULT_LANGUAGE)) {
                continue;
            }
            valueLangPairList.add(new ImmutablePair<>(value, languageCode));
        }
    }
}
