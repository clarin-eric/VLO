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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML implements CMDIDataProcessor {
    public static final String ENGLISH_LANGUAGE = "code:eng";
    public static final String DEFAULT_LANGUAGE = "code:und";

    private final Map<String, AbstractPostNormalizer> postProcessors;
    private final Boolean useLocalXSDCache;
    private static final java.util.regex.Pattern PROFILE_ID_PATTERN = java.util.regex.Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);


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

        if (facetMapping.getFacetConfigurations().isEmpty()) {
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
     * Sets default value if Null and if defined for the facet
     *
     * @param facetList list of processed facet configurations
     * @param cmdiData current CMDI data object
     */
    private void setDefaultIfNull(Collection<FacetConfiguration> facetList, CMDIData cmdiData) {
        for (FacetConfiguration facetConfig : facetList) {
            if (cmdiData.getDocField(facetConfig.getName()) == null && this.postProcessors.containsKey(facetConfig.getName()) && this.postProcessors.get(facetConfig.getName()).doesProcessNoValue()) {

            for(String value : this.postProcessors.get(facetConfig.getName()).process(null, cmdiData)) 
                insertFacetValue(cmdiData, facetConfig, ImmutableTriple.of(value, null, DEFAULT_LANGUAGE));

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

        /*
         * the map is necessary now to assure that English values are first in the list, even when the target is not the same as the origin facet
         */
        Map<FacetConfiguration, LinkedList<ValueStruct>> facetValuesMap = getFacetValuesMap(cmdiData, nav, facetMapping);
        
        writeValuesToDoc(cmdiData, facetValuesMap);
        setDefaultIfNull(facetValuesMap.keySet(), cmdiData);
    }
    
    /**
     *  
     * @param cmdiData
     * @param nav
     * @param facetMapping
     * @return a map of target facet configurations as and a List of ValueStruct as value. 
     * @throws VTDException
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    public Map<FacetConfiguration, LinkedList<ValueStruct>> getFacetValuesMap(CMDIData cmdiData, VTDNav nav, FacetMapping facetMapping) throws VTDException, URISyntaxException, UnsupportedEncodingException {
        Map<FacetConfiguration, LinkedList<ValueStruct>> facetValuesMap = new HashMap<FacetConfiguration, LinkedList<ValueStruct>>();
        /* 
         * The temporary storage in a map is necessary to assure that the values for a each target facet are sorted for English language first,  
        *  even when by the means of cross facet mapping the values are set while processing another facet than the target facet.
        */         
        final Collection<FacetConfiguration> facetConfigList = facetMapping.getFacetConfigurations();

        
        List<Triple<String, Integer, String>> valueLanguagePairList;

        for (FacetConfiguration facetConfig : facetConfigList) {
            
            valueLanguagePairList = new ArrayList<Triple<String, Integer, String>>();

            for (Pattern pattern : facetConfig.getPatterns()) {
                valueLanguagePairList.addAll(matchPattern(cmdiData, nav, pattern));
                if (!valueLanguagePairList.isEmpty() && !facetConfig.getAllowMultipleValues()) {
                    break;
                }
            }

            // using fallback patterns if extraction failed
            if (valueLanguagePairList.isEmpty()) {
                for (Pattern pattern : facetConfig.getFallbackPatterns()) {
                    valueLanguagePairList.addAll(matchPattern(cmdiData, nav, pattern));
                    if (!valueLanguagePairList.isEmpty() && !facetConfig.getAllowMultipleValues()) {
                        break;
                    }
                }
            }
            
            //processing the raw values for the origin facet
            for(Triple<String, Integer, String> valueLanguagePair: valueLanguagePairList)
                processRawValue(cmdiData, facetValuesMap, facetConfig, valueLanguagePair);
            
            //processing the same list of raw values for each derived facet
            for(FacetConfiguration derivedFacetConfig : facetConfig.getDerivedFacets()) {
                for(Triple<String, Integer, String> valueLanguagePair: valueLanguagePairList)
                    processRawValue(cmdiData, facetValuesMap, derivedFacetConfig, valueLanguagePair);
            }
            
        }
        
        return facetValuesMap;
    }
    
    /**
     * @param cmdiData
     * @param nav
     * @param pattern
     * @return
     * @throws VTDException
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     */
    public List<Triple<String,Integer,String>> matchPattern(CMDIData cmdiData, VTDNav nav, Pattern pattern) throws VTDException, URISyntaxException, UnsupportedEncodingException {
        final AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, extractXsd(nav));
        ap.selectXPath(pattern.getPattern());
        
        int index = ap.evalXPath();
        List<Triple<String, Integer, String>> valueIndexLanguageList = new ArrayList<>();
        
        while (index != -1) {


            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }

            
            valueIndexLanguageList.add(
                    ImmutableTriple.of(nav.toString(index), index, extractLanguageCode(nav))
                );
            
            
            Triple<String, Integer, String> vcl = extractValueConceptLink(nav);
            if (vcl != null) {
                ImmutablePair<String, String> vp = null;
                if (vcl.getLeft().contains("CCR_")) {
                    vp = CCR.getValue(new URI(vcl.getLeft()));
                } 
                else if (pattern.hasVocabulary()) {
                    vp = pattern.getVocabulary().getValue(new URI(vcl.getLeft()));
                }
                if (vp != null) {
                    final String v = (String) vp.getLeft();
                    final String l = (vp.getRight() != null ? postProcessors.get(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)).process((String) vp.getRight(), null).get(0) : DEFAULT_LANGUAGE);

                    valueIndexLanguageList.add(ImmutableTriple.of(v, vcl.getMiddle(), l));

                }
            }
            
            index = ap.evalXPath();
        }
        
        return valueIndexLanguageList;
    }
    
    protected Pair<String, String> getPair(VTDNav nav, int index) throws NavException {
        return ImmutablePair.of(nav.toString(index), extractLanguageCode(nav));
    }
    
    
    public void processRawValue(CMDIData cmdiData, Map<FacetConfiguration, LinkedList<ValueStruct>> targetValueMap, FacetConfiguration originFacetConfig, Triple<String, Integer, String> valueIndexLanguage) {
     // ignore non-English language names for facet LANGUAGE_CODE
        if (originFacetConfig.getName().equals(fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE)) && !valueIndexLanguage.getRight().equals(ENGLISH_LANGUAGE) && !valueIndexLanguage.getRight().equals(DEFAULT_LANGUAGE)) 
            return;
            
        boolean removeSourceValue = false;

        final List<String> postProcessed = postProcess(originFacetConfig.getName(), valueIndexLanguage.getLeft(), cmdiData);

        if (originFacetConfig.getConditionTargetSet() != null) {

            if (this.postProcessors.containsKey(originFacetConfig.getName()) && !(this.postProcessors
                    .get(originFacetConfig.getName()) instanceof AbstractPostNormalizerWithVocabularyMap)) {
                for (String postProcessedValue : postProcessed) {
                    for (TargetFacet target : originFacetConfig.getConditionTargetSet().getTargetsFor(postProcessedValue)) {
                        removeSourceValue |= target.getRemoveSourceValue();

                        setTargetValue(
                                targetValueMap, 
                                originFacetConfig, 
                                target.getFacetConfiguration(),
                                ImmutableTriple.of(target.getValue(), valueIndexLanguage.getMiddle(), ENGLISH_LANGUAGE),
                                target.getOverrideExistingValues());

                    }

                }
            } else {
                for (TargetFacet target : originFacetConfig.getConditionTargetSet()
                        .getTargetsFor(valueIndexLanguage.getLeft())) {
                    removeSourceValue |= target.getRemoveSourceValue();

                    setTargetValue(
                            targetValueMap, 
                            originFacetConfig, 
                            target.getFacetConfiguration(),
                            ImmutableTriple.of(target.getValue(), valueIndexLanguage.getMiddle(), ENGLISH_LANGUAGE), target.getOverrideExistingValues());

                }
            }

        }

        if (!removeSourceValue) { // positive 'removeSourceValue' means skip adding value to origin facet
            for (String postProcessedValue : postProcessed) {
                setTargetValue(
                        targetValueMap, 
                        originFacetConfig,
                        originFacetConfig,
                        ImmutableTriple.of(postProcessedValue, valueIndexLanguage.getMiddle(), valueIndexLanguage.getRight()), false);
            }

        }
        
    }
    

    private void writeValuesToDoc(CMDIData cmdiData, Map<FacetConfiguration, LinkedList<ValueStruct>> targetValueMap) {
        for(Entry<FacetConfiguration, LinkedList<ValueStruct>> entry : targetValueMap.entrySet()) {

            for(ValueStruct valueStruct : entry.getValue()) {
                insertFacetValue(cmdiData, entry.getKey(), valueStruct.getValueIndexLanguage());
                if(!(entry.getKey().getAllowMultipleValues()||entry.getKey().getMultilingual()))
                        break;
            }

        }
    }
    
    private void insertFacetValue( CMDIData cmdiData, FacetConfiguration facetConfig, Triple<String, Integer, String> valueIndexLanguage) {

        String fieldValue = valueIndexLanguage.getLeft().trim();
        
        if (facetConfig.getName().equals(fieldNameService.getFieldName(FieldKey.DESCRIPTION))) {
            fieldValue = "{" + valueIndexLanguage.getRight() + "}" + fieldValue;
        }

        cmdiData.addDocField(facetConfig.getName(), fieldValue, facetConfig.isCaseInsensitive());
    }
    
    public void setTargetValue(Map<FacetConfiguration, LinkedList<ValueStruct>> targetValueMap, FacetConfiguration originFacetConfig, FacetConfiguration targetFacetConfig, Triple<String, Integer, String> valueIndexLanguage, boolean overrideExistingValues) {
        if(overrideExistingValues) {
            LinkedList<ValueStruct> ll = new LinkedList<ValueStruct>() {

                /**
                 * A way to assure that an overriding value once set is not overridden again. Alternatively it could be stored and be written after
                 * having written all other values of the target facet
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public void addFirst(ValueStruct e) {}

                @Override
                public void addLast(ValueStruct e) {}
 
            };
            ll.add(new ValueStruct(originFacetConfig, targetFacetConfig, valueIndexLanguage));            
            
            targetValueMap.put(targetFacetConfig, ll);
            LOG.info("overriding existing value(s) in facet {} with value" + targetFacetConfig.getName(), valueIndexLanguage.getLeft());
        }
        else {
            LinkedList<ValueStruct> ll = targetValueMap.computeIfAbsent(targetFacetConfig, key -> new LinkedList<ValueStruct>());
            
            if(!targetFacetConfig.getAllowMultipleValues() && !targetFacetConfig.getMultilingual() && ll.size() > 0) {
                LOG.info("value for facet {} is set already. Since multiple value are not allowed value {} will be ignored!", targetFacetConfig.getName(), valueIndexLanguage.getLeft());
                return;
            }
            
            if(valueIndexLanguage.getRight().equals(ENGLISH_LANGUAGE)) {//prevents necessity to sort later
                ll.addFirst(new ValueStruct(originFacetConfig, targetFacetConfig, valueIndexLanguage)); 
            }
            else {
                ll.addLast(new ValueStruct(originFacetConfig, targetFacetConfig, valueIndexLanguage));
            }       
        }
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
     */
    private Triple<String, Integer, String> extractValueConceptLink(VTDNav nav) throws NavException {
        // extract English for ValueConceptLink if available
        Integer vclAttrIndex = nav.getAttrVal("cmd:ValueConceptLink");
        
        //using triple to have the index in the middle as in the other cases 
        Triple<String, Integer, String> vcl = null;
        if (vclAttrIndex != -1) {
            vcl = ImmutableTriple.of(nav.toString(vclAttrIndex).trim(), vclAttrIndex, null);
        }
        return vcl;
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
