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

import com.google.common.collect.Lists;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.Vocabulary;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizerWithVocabularyMap;
import static eu.clarin.cmdi.vlo.importer.processor.LanguageDefaults.DEFAULT_LANGUAGE;
import static eu.clarin.cmdi.vlo.importer.processor.LanguageDefaults.ENGLISH_LANGUAGE;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes values from a CMDI file into facets. Not to be reused!
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class FacetProcessorVTDXML implements FacetProcessor {

    private final Map<String, AbstractPostNormalizer> postProcessors;
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);

    private final Vocabulary CCR;
    private final FieldNameService fieldNameService;
    private final ValueWriter valueWriter;
    private final VTDNav nav;

    public FacetProcessorVTDXML(Map<String, AbstractPostNormalizer> postProcessors, VloConfig config, VLOMarshaller marshaller, ValueWriter valueWriter, FieldNameService fieldNameService, VTDNav nav) {
        this.postProcessors = postProcessors;
        this.CCR = new Vocabulary(config.getConceptRegistryUrl());
        this.fieldNameService = fieldNameService;
        this.valueWriter = valueWriter;
        this.nav = nav;
    }

    /**
     * Extracts facet values according to the facetMapping
     *
     * @param cmdiData representation of the CMDI document
     * @param facetMapping the facet mapping used to map meta data to facets
     * @throws java.net.URISyntaxException
     */
    @Override
    public void processFacets(CMDIData cmdiData, FacetMapping facetMapping) throws URISyntaxException, UnsupportedEncodingException, CMDIParsingException {
        try {
            FacetValuesMap facetValuesMap = getFacetValuesMap(cmdiData, nav, facetMapping);

            valueWriter.writeValuesToDoc(cmdiData, facetValuesMap);

            valueWriter.writeDefaultValues(cmdiData, facetMapping);
        } catch (VTDException ex) {
            throw new CMDIParsingException("VTD parsing exception while processing facets", ex);
        }
    }

    /**
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @param facetMapping A Map of facet-names (key)/ FacetConfiguration
     * (value)
     * @return A map of FacetConfigurations/Lists of ValueSets
     * @throws VTDException
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    private FacetValuesMap getFacetValuesMap(CMDIData cmdiData, VTDNav nav, FacetMapping facetMapping) throws VTDException, URISyntaxException, UnsupportedEncodingException {
        FacetValuesMap facetValuesMap = new FacetValuesMap();

        final Collection<FacetConfiguration> facetConfigList = facetMapping.getFacetConfigurations();

        boolean matchedPattern;

        for (FacetConfiguration facetConfig : facetConfigList) {
            matchedPattern = false;

            for (Pattern pattern : facetConfig.getPatterns()) {
                matchedPattern = matchPattern(cmdiData, facetValuesMap, nav, facetConfig, pattern);
                if (matchedPattern && !facetConfig.getAllowMultipleValues() && !facetConfig.getMultilingual()) {
                    break;
                }
            }

            // using fallback patterns if extraction failed
            if (!matchedPattern) {
                for (Pattern pattern : facetConfig.getFallbackPatterns()) {

                    if (matchPattern(cmdiData, facetValuesMap, nav, facetConfig, pattern) && !facetConfig.getAllowMultipleValues() && !facetConfig.getMultilingual()) {
                        break;
                    }
                }
            }

        }

        // reordering result pairs: prefer English content
        List<ValueSet> reorderedValueSetList;
        List<ValueSet> englishContentList;
        List<ValueSet> nonEnglishContentList;

        for (Map.Entry<String, List<ValueSet>> entry : facetValuesMap.entrySet()) {
            reorderedValueSetList = new ArrayList<>();
            englishContentList = new ArrayList<>();
            nonEnglishContentList = new ArrayList<>();

            for (int i = 0; i < entry.getValue().size(); i++) {
                ValueSet valueSet = entry.getValue().get(i);
                if (valueSet.getValueLanguagePair().getRight().equals(ENGLISH_LANGUAGE)) {
                    englishContentList.add(valueSet);
                } else {
                    nonEnglishContentList.add(valueSet);
                }
            }
            reorderedValueSetList.addAll(englishContentList);
            reorderedValueSetList.addAll(nonEnglishContentList);

            facetValuesMap.put(entry.getKey(), reorderedValueSetList);

        }

        /*      alternative approach for English first  
 *      facetValuesMap.forEach((facetConfig, valueSetList)-> 
                valueSetList.sort((first, second) -> first.getValueLanguagePair().getRight().equals(ENGLISH_LANGUAGE) && !second.getValueLanguagePair().getRight().equals(ENGLISH_LANGUAGE)?-1:0)
            );*/
        return facetValuesMap;
    }

    /**
     * @param cmdiData representation of the CMDI document
     * @param facetValuesMap A map of FacetConfigurations (key)/Lists of
     * ValueSets (value)
     * @param nav VTD Navigator
     * @param facetConfig FacetConfiguration to process
     * @param pattern Pattern to process (since a facet configuration contains
     * usually multiple patterns)
     * @return true, if there was a match for the pattern
     * @throws VTDException
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    private boolean matchPattern(CMDIData cmdiData, FacetValuesMap facetValuesMap, VTDNav nav, FacetConfiguration facetConfig, Pattern pattern) throws VTDException, URISyntaxException, UnsupportedEncodingException {
        final AutoPilot ap = new AutoPilot(nav);
        SchemaParsingUtil.setNameSpace(ap, SchemaParsingUtil.extractXsd(nav));
        ap.selectXPath(pattern.getPattern());

        int index = ap.evalXPath();

        LOG.trace("facet: {}, pattern: {}, VTDIndex: {}", facetConfig.getName(), pattern.getPattern(), index);

        boolean matchedPattern = false;

        while (index != -1) {
            matchedPattern = true;

            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }

            processRawValue(cmdiData, facetValuesMap, index, facetConfig, ImmutablePair.of(nav.toString(index), extractLanguageCode(nav)));

            processValueConceptLink(cmdiData, facetValuesMap, nav, facetConfig, pattern);

            index = ap.evalXPath();
        }

        return matchedPattern;
    }

    /**
     * @param nav
     * @return language code from xml:lang or default language code
     * @throws NavException
     */
    private String extractLanguageCode(VTDNav nav) throws NavException {
        // extract language code in xml:lang if available
        Integer langAttrIndex = nav.getAttrVal("xml:lang");
        String languageCode;
        if (langAttrIndex != -1) {
            languageCode = nav.toString(langAttrIndex).trim();
        } else {
            return DEFAULT_LANGUAGE;
        }

        return postProcessors.get(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).process(languageCode, null).get(0);
    }

    /**
     * @param nav
     * @return value concept link from element cmd:ValueConceptLink or Null
     * @throws NavException
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     * @throws XPathEvalException
     * @throws XPathParseException
     */
    private void processValueConceptLink(CMDIData cmdiData, FacetValuesMap facetValuesMap, VTDNav nav, FacetConfiguration facetConfig, Pattern pattern) throws NavException, XPathParseException, XPathEvalException, UnsupportedEncodingException, URISyntaxException {
        // extract english for ValueConceptLink if available
        Integer vclAttrIndex = nav.getAttrVal("cmd:ValueConceptLink");
        String vcl = null;
        if (vclAttrIndex != -1) {
            vcl = nav.toString(vclAttrIndex).trim();

            ImmutablePair<String, String> vp = null;
            if (vcl.contains("CCR_")) {
                vp = CCR.getValue(new URI(vcl));
            } else if (pattern.hasVocabulary()) {
                vp = pattern.getVocabulary().getValue(new URI(vcl));
            }
            if (vp != null) {
                final String v = (String) vp.getLeft();
                final String l = (vp.getRight() != null ? postProcessors.get(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).process((String) vp.getRight(), null).get(0) : DEFAULT_LANGUAGE);

                processRawValue(cmdiData, facetValuesMap, vclAttrIndex, facetConfig, ImmutablePair.of(v, l));

            }
        }

    }

    /**
     * Processing a raw value means to preserve it for import as it is, to
     * normalize it (post-normalizers) or to add/replace it by a predefined
     * value (value-mapping). A value can be mapped to the origin-facet as it is
     * defined in the facetConcepts file and/or to any other facet.
     *
     * @param cmdiData cmdiData representation of the CMDI document
     * @param facetValuesMap A map of FacetConfigurations (key)/Lists of
     * ValueSets (value)
     * @param vtdIndex VTD Navigator
     * @param facetConfig FacetConfiguration of the origin facet (which might
     * lead to different target facets)
     * @param valueLanguagePair Value/language Pair
     * @param isDerived Is derived facet
     */
    private void processRawValue(CMDIData cmdiData, FacetValuesMap facetValuesMap, int vtdIndex, FacetConfiguration facetConfig, Pair<String, String> valueLanguagePair) {
        if (facetConfig.getName().equals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)) && !valueLanguagePair.getRight().equals(ENGLISH_LANGUAGE) && !valueLanguagePair.getRight().equals(DEFAULT_LANGUAGE)) {
            return;
        }
        boolean removeSourceValue = false;

        final List<String> postProcessed = postProcess(facetConfig.getName(), valueLanguagePair.getLeft(), facetValuesMap);
        
        if (facetConfig.getConditionTargetSet() != null) {

            if (this.postProcessors.containsKey(facetConfig.getName()) && !(this.postProcessors
                    .get(facetConfig.getName()) instanceof AbstractPostNormalizerWithVocabularyMap)) {
                for (String postProcessedValue : postProcessed) {
                    for (TargetFacet target : facetConfig.getConditionTargetSet().getTargetsFor(postProcessedValue)) {
                        removeSourceValue |= target.getRemoveSourceValue();

                        ValueSet valueSet = new ValueSet(vtdIndex, facetConfig, target, ImmutablePair.of(target.getValue(), ENGLISH_LANGUAGE), false, true);

                        setTargetValue(facetValuesMap, valueSet);

                    }

                }
            } else {
                for (TargetFacet target : facetConfig.getConditionTargetSet()
                        .getTargetsFor(valueLanguagePair.getLeft())) {
                    removeSourceValue |= target.getRemoveSourceValue();

                    ValueSet valueSet = new ValueSet(vtdIndex, facetConfig, target, ImmutablePair.of(target.getValue(), ENGLISH_LANGUAGE), false, true);

                    setTargetValue(facetValuesMap, valueSet);

                }
            }

        }

        if (!removeSourceValue) { // positive 'removeSourceValue' means skip adding value to origin facet
            for (String postProcessedValue : postProcessed) {

                ValueSet valueSet = new ValueSet(vtdIndex, facetConfig, new TargetFacet(facetConfig, ""), ImmutablePair.of(postProcessedValue, valueLanguagePair.getRight()), false, false);
                setTargetValue(facetValuesMap, valueSet);
            }

        }

        // insert post-processed values into derived facet(s) if configured
        for (FacetConfiguration derivedFacetConfig : facetConfig.getDerivedFacets()) {
            for (String postProcessedValue : postProcessed) {
                for (String derivedValue : postProcess(derivedFacetConfig.getName(), postProcessedValue, facetValuesMap)) {
                    ValueSet valueSet = new ValueSet(vtdIndex, facetConfig, new TargetFacet(derivedFacetConfig, ""), ImmutablePair.of(derivedValue, valueLanguagePair.getRight()), true, false);
                    setTargetValue(facetValuesMap, valueSet);
                }
            }
        }

    }

    /**
     * @param facetValuesMap A map of FacetConfigurations (key)/Lists of
     * ValueSets (value)
     * @param valueSet
     */
    private void setTargetValue(FacetValuesMap facetValuesMap, ValueSet valueSet) {
        if (valueSet.getTargetFacet().getOverrideExistingValues()) {
            facetValuesMap.put(valueSet.getTargetFacetName(), Lists.newArrayList(valueSet));
        }
        else {
            final List<ValueSet> existingValueSets = Optional.ofNullable(
                    facetValuesMap.get(valueSet.getTargetFacetName()))
                    .orElse(new ArrayList<>());

            if (existingValueSets.size() > 0 && (existingValueSets.get(0).getTargetFacet().getOverrideExistingValues() || (!valueSet.getTargetFacet().getFacetConfiguration().getAllowMultipleValues() && !valueSet.getTargetFacet().getFacetConfiguration().getMultilingual()))) {
                LOG.debug("value {} ignored since facet {} has either defined an overriding value or doensn't allow multiple values",
                        valueSet.getValue(), valueSet.getTargetFacetName());
            } else {
                facetValuesMap.addToTarget(valueSet);
            }
        }
    }

    /**
     * Applies registered PostProcessor to extracted values
     *
     * @param facetName name of the facet for which value was extracted
     * @param extractedValue extracted value from CMDI file
     * @return value after applying matching PostProcessor or the original value
     * if no PostProcessor was registered for the facet
     */
    private List<String> postProcess(String facetName, String extractedValue, FacetValuesMap facetValuesMap) {
        List<String> resultList = new ArrayList<>();
        if (postProcessors.containsKey(facetName)) {
            AbstractPostNormalizer processor = postProcessors.get(facetName);
            resultList = processor.process(extractedValue, facetValuesMap);
        } else {
            resultList.add(extractedValue);
        }
        return resultList;
    }
}
