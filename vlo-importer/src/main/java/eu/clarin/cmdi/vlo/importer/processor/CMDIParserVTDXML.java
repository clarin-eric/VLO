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
import java.util.HashMap;
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

        
        Map<FacetConfiguration, List<Pair<String,String>>> rawValuesMap = getRawValuesMap(nav, facetMapping.getFacets());
        
        mapToTaget(rawValuesMap, cmdiData);

        
    }
    
    /**
     * @param nav  VTD Navigator
     * @param facetList
     * @return map of facet-configurations and list of value/language-pairs
     * @throws VTDException
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public Map<FacetConfiguration, List<Pair<String,String>>> getRawValuesMap(VTDNav nav, Collection<FacetConfiguration> facetList) throws VTDException, UnsupportedEncodingException, URISyntaxException {
        Map<FacetConfiguration, List<Pair<String,String>>> rawValuesMap = new HashMap<FacetConfiguration, List<Pair<String,String>>>();
        
        List<Pair<String,String>> valueLangPairList;
            
        for (FacetConfiguration facetConfig : facetList) { //looping through facetConfigurations
            valueLangPairList = new ArrayList<Pair<String, String>>();
          
            for (Pattern pattern : facetConfig.getPatterns()) { //looping through patterns based on concept definitions
                  valueLangPairList.addAll(getRawValues(nav, pattern));
                  
                  if (!valueLangPairList.isEmpty() && !facetConfig.getAllowMultipleValues()) 
                      break;
            }
        
            // using fallback patterns if extraction failed
            if (valueLangPairList.isEmpty()) {
                  for (Pattern pattern : facetConfig.getFallbackPatterns()) { //looping through default patterns
                      valueLangPairList.addAll(getRawValues(nav, pattern));
                      
                      if (!valueLangPairList.isEmpty() && !facetConfig.getAllowMultipleValues()) 
                          break;

                  }
            }
            
            rawValuesMap.put(facetConfig, valueLangPairList);

        }  
        
        return rawValuesMap;
    }
    
    /**
     * @param nav
     * @param pattern
     * @return
     * @throws VTDException
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    private List<Pair<String, String>> getRawValues(VTDNav nav, Pattern pattern) throws VTDException, UnsupportedEncodingException, URISyntaxException{
        final AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, extractXsd(nav));
        ap.selectXPath(pattern.getPattern());

        int index = ap.evalXPath();
        List<Pair<String, String>> valueLangPairList = new ArrayList<>();
        
        // extract (almost) all values with their language code
        while (index != -1) {

            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }
            
            valueLangPairList.add(new ImmutablePair<>(nav.toString(index), extractLanguageCode(nav)));
            
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
                    final String l = (vp.getRight() != null ? postProcessors.get(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).process((String) vp.getRight(), null).get(0) : DEFAULT_LANGUAGE);

                        valueLangPairList.add(new ImmutablePair<String, String>(v, l));
                }

            }
            
            
            index = ap.evalXPath();

        }//end while
        
        return valueLangPairList;

    }
    
    /**
     * @param rawValuesMap Map of facet configurations and a list of value/language-pairs
     * @param cmdiData representation of the CMDI document
     */
    public void mapToTaget(Map<FacetConfiguration, List<Pair<String,String>>> rawValuesMap, CMDIData cmdiData) {
        boolean removeSourceValue = false;
        List<TargetFacet> overridingTargets = new ArrayList<TargetFacet>();
        
        
        for(Map.Entry<FacetConfiguration, List<Pair<String,String>>> entry : rawValuesMap.entrySet()) {
            List<Pair<String,String>> valueLangPairList = new ArrayList<Pair<String,String>>();
            FacetConfiguration facetConfig = entry.getKey();
            for (Pair<String,String> rawValueLanguagePair : entry.getValue()) {
                List<String> postProcessed = postProcess(facetConfig.getName(), rawValueLanguagePair.getLeft(), cmdiData);
                
                if (this.postProcessors.containsKey(facetConfig.getName()) && !(this.postProcessors.get(facetConfig.getName()) instanceof AbstractPostNormalizerWithVocabularyMap)) {
                    for (String postProcessedValue : postProcessed) {
                        removeSourceValue |= processValueMapping(facetConfig, postProcessedValue, cmdiData, overridingTargets);
                    }
                } 
                else {
                    removeSourceValue |= processValueMapping(facetConfig, rawValueLanguagePair.getLeft(), cmdiData, overridingTargets);
                }

                if (!removeSourceValue) { //positive 'removeSourceValue' means skip processing original value

                    addValuesToList(facetConfig.getName(), postProcessed, valueLangPairList, rawValueLanguagePair.getRight());
                }
            }
            
            // reordering result pairs: prefer English content
            valueLangPairList.sort((first, second) -> first.equals(ENGLISH_LANGUAGE) && !second.equals(ENGLISH_LANGUAGE)?-1:0);

            // insert values into original facet
            insertFacetValues(facetConfig, valueLangPairList, cmdiData, false);

            // insert post-processed values into derived facet(s) if configured
            for (FacetConfiguration derivedFacetConfig : facetConfig.getDerivedFacets()) {
                final List<Pair<String, String>> derivedValueLangPairList = new ArrayList<>();
                
                for (Pair<String,String> rawValueLanguagePair : entry.getValue()) {
                    for (String derivedValue : postProcess(derivedFacetConfig.getName(), rawValueLanguagePair.getLeft(), cmdiData)) {
                        derivedValueLangPairList.add(new ImmutablePair<>(derivedValue, rawValueLanguagePair.getRight()));
                    }
                }
                insertFacetValues(derivedFacetConfig, derivedValueLangPairList, cmdiData, false);
            }

            
        }
        
        processOverrideValues(overridingTargets, cmdiData);

        setDefaultIfNull(rawValuesMap.keySet(), cmdiData);
        
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
