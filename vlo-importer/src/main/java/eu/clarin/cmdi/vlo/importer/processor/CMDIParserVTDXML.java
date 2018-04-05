package eu.clarin.cmdi.vlo.importer.processor;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.CmdConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.ResourceStructureGraph;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.Vocabulary;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizerWithVocabularyMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML implements CMDIDataProcessor {

    private final Map<String, AbstractPostNormalizer> postProcessors;
    private final Boolean useLocalXSDCache;
    private static final java.util.regex.Pattern PROFILE_ID_PATTERN = java.util.regex.Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);

    private static final String ENGLISH_LANGUAGE = "code:eng";
    private static final String DEFAULT_LANGUAGE = "code:und";
    private final SelfLinkExtractor selfLinkExtractor = new SelfLinkExtractorImpl();
    private final Vocabulary CCR;
    private final FacetMappingFactory facetMappingFactory;
    private final FieldNameServiceImpl fieldNameService;

    public CMDIParserVTDXML(Map<String, AbstractPostNormalizer> postProcessors, VloConfig config, FacetMappingFactory facetMappingFactory, VLOMarshaller marshaller, Boolean useLocalXSDCache) {
        this.postProcessors = postProcessors;
        this.useLocalXSDCache = useLocalXSDCache;
        this.facetMappingFactory = facetMappingFactory;
        this.CCR = new Vocabulary(config.getConceptRegistryUrl());
        this.fieldNameService = new FieldNameServiceImpl(config);
    }

    @Override
    public CMDIData process(File file, ResourceStructureGraph resourceStructureGraph) throws VTDException, IOException, URISyntaxException {
        final CMDIData cmdiData = new CMDIData(this.fieldNameService);
        final VTDGen vg = new VTDGen();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            vg.setDoc(IOUtils.toByteArray(fileInputStream));
            vg.parse(true);
        }

        final VTDNav nav = vg.getNav();
        final FacetMapping facetMapping = getFacetMapping(nav.cloneNav());

        if (facetMapping.getFacets().isEmpty()) {
            LOG.error("Problems mapping facets for file: {}", file.getAbsolutePath());
        }

        nav.toElement(VTDNav.ROOT);
        processResources(cmdiData, nav, resourceStructureGraph);
        processFacets(cmdiData, nav, facetMapping);
        return cmdiData;
    }

    @Override
    public String extractMdSelfLink(File file) throws IOException {
        return selfLinkExtractor.extractMdSelfLink(file);
    }

    /**
     * Setting namespace for Autopilot ap
     *
     * @param ap
     */
    private void setNameSpace(AutoPilot ap, String profileId) {
        ap.declareXPathNameSpace("cmd", CmdConstants.CMD_NAMESPACE);
        if (profileId != null) {
            ap.declareXPathNameSpace("cmdp", "http://www.clarin.eu/cmd/1/profiles/" + profileId);
        }
    }

    /**
     * Extracts valid XML patterns for all facet definitions
     *
     * @param nav VTD Navigator
     * @return the facet mapping used to map meta data to facets
     * @throws VTDException
     */
    private FacetMapping getFacetMapping(VTDNav nav) throws VTDException {
        final String profileId = extractXsd(nav);
        if (profileId == null) {
            throw new RuntimeException("Cannot get xsd schema so cannot get a proper mapping. Parse failed!");
        }

        return facetMappingFactory.getFacetMapping(profileId, useLocalXSDCache);
    }

    /**
     * Try two approaches to extract the XSD schema information from the CMDI
     * file
     *
     * @param nav VTD Navigator
     * @return ID of CMDI schema, or null if neither the CMDI header nor the
     * XMLSchema-instance's attributes contained the information
     * @throws VTDException
     */
    public String extractXsd(VTDNav nav) throws VTDException {
        String profileID = getProfileIdFromHeader(nav);
        if (profileID == null) {
            profileID = getProfileIdFromSchemaLocation(nav);
        }
        return profileID;
    }

    /**
     * Extract XSD schema information from CMDI header (using element
     * //Header/MdProfile)
     *
     * @param nav VTD Navigator
     * @return ID of CMDI schema, or null if content of //Header/MdProfile
     * element could not be read
     * @throws XPathParseException
     * @throws XPathEvalException
     * @throws NavException
     */
    private String getProfileIdFromHeader(VTDNav nav) throws XPathParseException, XPathEvalException, NavException {
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, null);
        ap.selectXPath("/cmd:CMD/cmd:Header/cmd:MdProfile/text()");
        int index = ap.evalXPath();
        String profileId = null;
        if (index != -1) {
            profileId = nav.toString(index).trim();
        }
        return profileId;
    }

    /**
     * Extract XSD schema information from schemaLocation or
     * noNamespaceSchemaLocation attributes
     *
     * @param nav VTD Navigator
     * @return ID of CMDI schema, or null if attributes don't exist
     * @throws NavException
     */
    private String getProfileIdFromSchemaLocation(VTDNav nav) throws NavException {
        String result = null;
        nav.toElement(VTDNav.ROOT);
        int index = nav.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
        if (index != -1) {
            String schemaLocation = nav.toNormalizedString(index);
            String[] schemaLocationArray = schemaLocation.split(" ");
            result = schemaLocationArray[schemaLocationArray.length - 1];
        } else {
            index = nav.getAttrValNS("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation");
            if (index != -1) {
                result = nav.toNormalizedString(index);
            }
        }

        // extract profile ID
        if (result != null) {
            Matcher m = PROFILE_ID_PATTERN.matcher(result);
            if (m.find()) {
                return m.group(1);
            }
        }
        return null;
    }

    /**
     * Extract ResourceProxies from ResourceProxyList
     *
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @throws VTDException
     */
    private void processResources(CMDIData cmdiData, VTDNav nav, ResourceStructureGraph resourceStructureGraph) throws VTDException {
        AutoPilot mdSelfLink = new AutoPilot(nav);
        setNameSpace(mdSelfLink, null);
        mdSelfLink.selectXPath("/cmd:CMD/cmd:Header/cmd:MdSelfLink");
        String mdSelfLinkString = mdSelfLink.evalXPathToString();
        if (resourceStructureGraph != null) {
            resourceStructureGraph.addResource(mdSelfLinkString);
        }

        AutoPilot resourceProxy = new AutoPilot(nav);
        setNameSpace(resourceProxy, null);
        resourceProxy.selectXPath("/cmd:CMD/cmd:Resources/cmd:ResourceProxyList/cmd:ResourceProxy");

        AutoPilot resourceRef = new AutoPilot(nav);
        setNameSpace(resourceRef, null);
        resourceRef.selectXPath("cmd:ResourceRef");

        AutoPilot resourceType = new AutoPilot(nav);
        setNameSpace(resourceType, null);
        resourceType.selectXPath("cmd:ResourceType");

        AutoPilot resourceMimeType = new AutoPilot(nav);
        setNameSpace(resourceMimeType, null);
        resourceMimeType.selectXPath("cmd:ResourceType/@mimetype");

        while (resourceProxy.evalXPath() != -1) {
            String ref = resourceRef.evalXPathToString();
            String type = resourceType.evalXPathToString();
            String mimeType = resourceMimeType.evalXPathToString();

            if (!ref.equals("") && !type.equals("")) {
                // note that the mime type could be empty
                cmdiData.addResource(ref, type, mimeType);
            }

            // resource hierarchy information?
            if (resourceStructureGraph != null && type.toLowerCase().equals("metadata")) {
                resourceStructureGraph.addEdge(ref, mdSelfLinkString);
            }
        }
    }

    /**
     * Extracts facet values according to the facetMapping
     *
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @param facetMapping the facet mapping used to map meta data to facets
     * @throws VTDException
     */
    private void processFacets(CMDIData cmdiData, VTDNav nav, FacetMapping facetMapping) throws VTDException, URISyntaxException, UnsupportedEncodingException {
        final Collection<FacetConfiguration> facetList = facetMapping.getFacets();

        List<TargetFacet> overridingTargets = new ArrayList<TargetFacet>();

//        final List<String> processedFacets = new ArrayList<>(facetList.size());
        for (FacetConfiguration config : facetList) {

//            processedFacets.add(config.getName());
            boolean matchedPattern = false;
            List<Pattern> patterns = config.getPatterns();
            for (Pattern pattern : patterns) {
                matchedPattern = matchPattern(cmdiData, nav, config, pattern, overridingTargets);
                if (matchedPattern && !config.getAllowMultipleValues()) {
                    break;
                }
            }

            // using fallback patterns if extraction failed
            if (!matchedPattern) {
                for (Pattern pattern : config.getFallbackPatterns()) {
                    matchedPattern = matchPattern(cmdiData, nav, config, pattern, overridingTargets);
                    if (matchedPattern && !config.getAllowMultipleValues()) {
                        break;
                    }
                }
            }

        }

        processOverrideValues(overridingTargets, cmdiData);

        setDefaultIfNull(facetList, cmdiData);
    }

    /**
     * @param overridingTargets list of buffered target facets
     * @param cmdiData current CMDI data object
     */
    private void processOverrideValues(List<TargetFacet> overridingTargets, CMDIData cmdiData) {
        if(LOG.isDebugEnabled() && !overridingTargets.isEmpty()) {
            LOG.debug("Applying {} values set to override existing values in the target", overridingTargets.size());
        }
        overridingTargets.forEach(targetFacet -> {
            ArrayList<Pair<String, String>> valueList = new ArrayList<Pair<String, String>>();
            valueList.add(new ImmutablePair<String, String>(targetFacet.getValue(), DEFAULT_LANGUAGE));

            LOG.debug("Value mapping: inserting replacement value '{}' in field {}", targetFacet.getFacetConfiguration().getName(), targetFacet.getValue());
            insertFacetValues(targetFacet.getFacetConfiguration(), valueList, cmdiData, true);
        });
    }

    /**
     * Sets default value if Null and if defined for the facet
     *
     * @param facetList list of processed facet configurations
     * @param cmdiData current CMDI data object
     */
    private void setDefaultIfNull(Collection<FacetConfiguration> facetList, CMDIData cmdiData) {

        for (FacetConfiguration facetConfig : facetList) {
            if (cmdiData.getDocField(facetConfig.getName()) == null && this.postProcessors.containsKey(facetConfig.getName()) && this.postProcessors.get(facetConfig.getName()).doesProcessNoValue()) {
                final ArrayList<Pair<String, String>> valueLangPairList = new ArrayList<>();
                addValuesToList(facetConfig.getName(), this.postProcessors.get(facetConfig.getName()).process(null, cmdiData), valueLangPairList, DEFAULT_LANGUAGE);
                insertFacetValues(facetConfig, valueLangPairList, cmdiData, false);
            }
        }
    }

    /**
     * Extracts content from CMDI file for a specific facet based on a single
     * XPath expression
     *
     * @param cmdiData representation of the CMDI document
     * @param nav VTD Navigator
     * @param config facet configuration
     * @param pattern XPath expression of this facet
     * @return pattern matched a node in the CMDI file?
     * @throws VTDException
     */
    private boolean matchPattern(CMDIData cmdiData, VTDNav nav, FacetConfiguration facetConfig, Pattern pattern, List<TargetFacet> overridingTargets) throws VTDException, URISyntaxException, UnsupportedEncodingException {
        final AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, extractXsd(nav));
        ap.selectXPath(pattern.getPattern());

        boolean matchedPattern = false;
        boolean removeSourceValue = false;

        int index = ap.evalXPath();
        List<Pair<String, String>> valueLangPairList = new ArrayList<>();

        // extract (almost) all values with their language code
        while (index != -1) {
            matchedPattern = true;
            removeSourceValue = false;

            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }
            final String value = nav.toString(index);
            final String languageCode = extractLanguageCode(nav);

            final List<String> postProcessed = postProcess(facetConfig.getName(), value, cmdiData);

            if (this.postProcessors.containsKey(facetConfig.getName()) && !(this.postProcessors.get(facetConfig.getName()) instanceof AbstractPostNormalizerWithVocabularyMap)) {
                for (String postProcessedValue : postProcessed) {
                    removeSourceValue |= processValueMapping(facetConfig, postProcessedValue, cmdiData, overridingTargets);
                }
            } else {
                removeSourceValue |= processValueMapping(facetConfig, value, cmdiData, overridingTargets);
            }

            if (!removeSourceValue) { //positive 'removeSourceValue' means skip processing original value

                addValuesToList(facetConfig.getName(), postProcessed, valueLangPairList, languageCode);

                String vcl = extractValueConceptLink(nav);
                if (vcl != null) {
                    ImmutablePair<String, String> vp = null;
                    if (vcl.contains("CCR_")) {
                        vp = CCR.getValue(new URI(vcl));
                    } else if (pattern.hasVocabulary()) {
                        vp = pattern.getVocabulary().getValue(new URI(vcl));
                    }
                    if (vp != null) {
                        final String v = (String) vp.getLeft();
                        final String l = (vp.getRight() != null ? postProcessors.get(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).process((String) vp.getRight(), cmdiData).get(0) : DEFAULT_LANGUAGE);
                        for (String pv : postProcess(facetConfig.getName(), v, cmdiData)) {
                            valueLangPairList.add(new ImmutablePair<>(pv, l));
                        }
                    }
                }
            }

            index = ap.evalXPath();

        }//end while

        // return if no result was found or accepted
        if (!matchedPattern || valueLangPairList.isEmpty()) {
            return matchedPattern;
        }

        // reordering result pairs: prefer English content
        List<Pair<String, String>> reorderedValueLangPairList = new ArrayList<>();
        List<Integer> englishContentIndices = new ArrayList<>();
        List<Integer> nonEnglishContentIndices = new ArrayList<>();
        for (int i = 0; i < valueLangPairList.size(); i++) {
            Pair<String, String> valueLangPair = valueLangPairList.get(i);
            if (valueLangPair.getRight().equals(ENGLISH_LANGUAGE)) {
                englishContentIndices.add(i);
            } else {
                nonEnglishContentIndices.add(i);
            }
        }
        englishContentIndices.stream().forEach((i) -> {
            reorderedValueLangPairList.add(new ImmutablePair<>(valueLangPairList.get(i).getLeft(), valueLangPairList.get(i).getRight()));
        });
        nonEnglishContentIndices.stream().forEach((i) -> {
            reorderedValueLangPairList.add(new ImmutablePair<>(valueLangPairList.get(i).getLeft(), valueLangPairList.get(i).getRight()));
        });

        // insert values into original facet
        insertFacetValues(facetConfig, reorderedValueLangPairList, cmdiData, false);

        // insert post-processed values into derived facet(s) if configured
        for (FacetConfiguration derivedFacet : facetConfig.getDerivedFacets()) {
            final List<Pair<String, String>> derivedValueLangPairList = new ArrayList<>();
            for (Pair<String, String> valueLangPair : reorderedValueLangPairList) {
                for (String derivedValue : postProcess(derivedFacet.getName(), valueLangPair.getLeft(), cmdiData)) {
                    derivedValueLangPairList.add(new ImmutablePair<>(derivedValue, valueLangPair.getRight()));
                }
            }
            insertFacetValues(derivedFacet, derivedValueLangPairList, cmdiData, false);
        }

        return matchedPattern;
    }

    /**
     * Process value mappings and determine whether the source value should be
     * processed or removed (excluded from processing)
     *
     * @param facetConfig facet configuration
     * @param value either post-processed or origin value
     * @param languageCode from xml:lang or default language
     * @param cmdiData representation of the CMDI document
     * @return remove source value?
     */
    private boolean processValueMapping(FacetConfiguration facetConfig, String value, CMDIData cmdiData, List<TargetFacet> overridingTargets) {
        boolean removeSourceValue = false;

        if (facetConfig.getConditionTargetSet() == null) // no set defined for the facet
        {
            return false;
        }
        
        LOG.trace("Processing value mapping for facet {}, value {}", facetConfig.getName(), value);

        for (TargetFacet target : facetConfig.getConditionTargetSet().getTargetsFor(value)) {
            removeSourceValue |= target.getRemoveSourceValue();
            
            if (target.getOverrideExistingValues()) {
                LOG.debug("Saving to override later: [{}: '{}'] -> [{}: '{}']", facetConfig.getName(), value, target.getFacetConfiguration().getName(), target.getValue());
                overridingTargets.add(target);
            } else {
                ArrayList<Pair<String, String>> valueList = new ArrayList<Pair<String, String>>();
                valueList.add(new ImmutablePair<String, String>(target.getValue(), DEFAULT_LANGUAGE));
                
                LOG.debug("Value mapping: applying mapping [{}: '{}'] -> [{}: '{}'] (override existing: {})", facetConfig.getName(), value, target.getFacetConfiguration().getName(), target.getValue(), target.getOverrideExistingValues());
                insertFacetValues(target.getFacetConfiguration(), valueList, cmdiData, target.getOverrideExistingValues());
            }

        }

        return removeSourceValue;

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

    private void addValuesToList(String facetName, final List<String> values, List<Pair<String, String>> valueLangPairList, final String languageCode) {
        for (String value : values) {
            // ignore non-English language names for facet LANGUAGE_CODE
            if (facetName.equals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)) && !languageCode.equals(ENGLISH_LANGUAGE) && !languageCode.equals(DEFAULT_LANGUAGE)) {
                continue;
            }
            valueLangPairList.add(new ImmutablePair<>(value, languageCode));
        }
    }

    /**
     * @param nav
     * @return value concept link from element cmd:ValueConceptLink or Null
     * @throws NavException
     */
    private String extractValueConceptLink(VTDNav nav) throws NavException {
        // extract english for ValueConceptLink if available
        Integer vclAttrIndex = nav.getAttrVal("cmd:ValueConceptLink");
        String vcl = null;
        if (vclAttrIndex != -1) {
            vcl = nav.toString(vclAttrIndex).trim();
        }
        return vcl;
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

    /**
     * Applies registered PostProcessor to extracted values
     *
     * @param facetName name of the facet for which value was extracted
     * @param extractedValue extracted value from CMDI file
     * @return value after applying matching PostProcessor or the original value
     * if no PostProcessor was registered for the facet
     */
    private List<String> postProcess(String facetName, String extractedValue, CMDIData cmdiData) {
        List<String> resultList = new ArrayList<>();
        if (postProcessors.containsKey(facetName)) {
            AbstractPostNormalizer processor = postProcessors.get(facetName);
            resultList = processor.process(extractedValue, cmdiData);
        } else {
            resultList.add(extractedValue);
        }
        return resultList;
    }
}
