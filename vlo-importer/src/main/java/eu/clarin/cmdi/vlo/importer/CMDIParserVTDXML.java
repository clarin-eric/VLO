package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.CmdConstants;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CFMCondition.FacetValuePair;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.PostProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML implements CMDIDataProcessor {

    private final Map<String, PostProcessor> postProcessors;
    private final Boolean useLocalXSDCache;
    private static final java.util.regex.Pattern PROFILE_ID_PATTERN = java.util.regex.Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);
    
    private static final String ENGLISH_LANGUAGE = "code:eng";
    private static final String DEFAULT_LANGUAGE = "code:und";
    private final SelfLinkExtractor selfLinkExtractor = new SelfLinkExtractorImpl();
    private final VloConfig config;
    private final Vocabulary CCR;
    private final FacetMappingFactory facetMappingFactory;
    private final VLOMarshaller marshaller;
    
    public CMDIParserVTDXML(Map<String, PostProcessor> postProcessors, VloConfig config, FacetMappingFactory facetMappingFactory, VLOMarshaller marshaller, Boolean useLocalXSDCache) {
        this.postProcessors = postProcessors;
        this.useLocalXSDCache = useLocalXSDCache;
        this.config = config;
        this.facetMappingFactory = facetMappingFactory;
        this.marshaller = marshaller;
        this.CCR = new Vocabulary(config.getConceptRegistryUrl());
    }

    @Override
    public CMDIData process(File file, ResourceStructureGraph resourceStructureGraph) throws VTDException, IOException, URISyntaxException {
        final CMDIData cmdiData = new CMDIData();
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
        
        final String facetConceptsFile = config.getFacetConceptsFile();
        return facetMappingFactory.getFacetMapping(facetConceptsFile, profileId, useLocalXSDCache);
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
    String extractXsd(VTDNav nav) throws VTDException {
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
        final List<FacetConfiguration> facetList = facetMapping.getFacets();
        final List<String> processedFacets = new ArrayList<>(facetList.size());
        for (FacetConfiguration config : facetList) {
            processedFacets.add(config.getName());
            boolean matchedPattern = false;
            List<Pattern> patterns = config.getPatterns();
            for (Pattern pattern : patterns) {
                matchedPattern = matchPattern(cmdiData, nav, config, pattern, config.getAllowMultipleValues() || config.getMultilingual());
                if (matchedPattern && !config.getAllowMultipleValues()) {
                    break;
                }
            }

            // using fallback patterns if extraction failed
            if (!matchedPattern) {
                for (Pattern pattern : config.getFallbackPatterns()) {
                    matchedPattern = matchPattern(cmdiData, nav, config, pattern, config.getAllowMultipleValues() || config.getMultilingual());
                    if (matchedPattern && !config.getAllowMultipleValues()) {
                        break;
                    }
                }
            }

            if (!matchedPattern) {
                //no matching value
                processNoMatch(config.getName(), config.getAllowMultipleValues(), config.isCaseInsensitive(), cmdiData);
            }
        }

        handleUnprocessedFields(processedFacets, cmdiData);
    }

    private void handleUnprocessedFields(final List<String> processedFields, CMDIData cmdiData) {
        // check for unprocessed facets that allow 'no value' post processing
        Map<String, FacetConceptMapping.FacetConcept> facetConceptMap = null;
        for (String fieldWithPostProcessor : postProcessors.keySet()) {
            if (!processedFields.contains(fieldWithPostProcessor)) {
                if (postProcessors.get(fieldWithPostProcessor).doesProcessNoValue()) {
                    //get properties from facet concept definition
                    if (facetConceptMap == null) {
                        final FacetConceptMapping facetConceptMapping = marshaller.getFacetConceptMapping(config.getFacetConceptsFile());
                        facetConceptMap = facetConceptMapping.getFacetConceptMap();
                    }
                    final FacetConceptMapping.FacetConcept facetConcept = facetConceptMap.get(fieldWithPostProcessor);
                    if (facetConcept == null) {
                        LOG.warn("Postprocessor defined for field not defined in facet concepts definition: {}. Skipping processing of unmapped field.", fieldWithPostProcessor);
                    } else {
                        processNoMatch(fieldWithPostProcessor, facetConcept.isAllowMultipleValues(), facetConcept.isCaseInsensitive(), cmdiData);
                    }
                }
            }
        }
    }

    /**
     * Performs post processing in case no value was found for a specific facet
     *
     * @param facetName facet
     * @param allowMultipleValues allow multiple values?
     * @param caseInsensitive case insensitive?
     * @param cmdiData current CMDI data object
     */
    private void processNoMatch(String facetName, boolean allowMultipleValues, boolean caseInsensitive, CMDIData cmdiData) {
        if (postProcessors.containsKey(facetName) && postProcessors.get(facetName).doesProcessNoValue()) {
            //post process 'no value'
            final List<String> postProcessed = postProcess(facetName, null, cmdiData);
            if (postProcessed != null && !postProcessed.isEmpty()) {
                final ArrayList<Pair<String, String>> valueLangPairList = new ArrayList<>();
                addValuesToList(facetName, postProcessed, valueLangPairList, DEFAULT_LANGUAGE);
                insertFacetValues(facetName, valueLangPairList, cmdiData, allowMultipleValues, caseInsensitive, false);
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
     * @param pattern XPath expression
     * @param allowMultipleValues information if multiple values are allowed in
     * this facet
     * @return pattern matched a node in the CMDI file?
     * @throws VTDException
     */
    private boolean matchPattern(CMDIData cmdiData, VTDNav nav, FacetConfiguration config, Pattern pattern, Boolean allowMultipleValues) throws VTDException, URISyntaxException, UnsupportedEncodingException {
        final AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, extractXsd(nav));
        ap.selectXPath(pattern.getPattern());

        boolean matchedPattern = false;
        int index = ap.evalXPath();
        List<Pair<String, String>> valueLangPairList = new ArrayList<>();

        // extract (almost) all values with their language code
        while (index != -1) {
            matchedPattern = true;
            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }
            final String value = nav.toString(index);
            final String languageCode = extractLanguageCode(nav);
            
            
            //implementation of cross facet mapping (cfm)
            if(!config.getConditions().isEmpty()){
            	
            	for(CFMCondition condition : config.getConditions()){
            		if(condition.getIfValue().equals(value)){ //the value matches the condition-value
            			for(FacetValuePair fvp : condition.getFacetValuePairs()){
            				ArrayList<Pair<String,String>> cfmList = new ArrayList<Pair<String,String>>();
            				cfmList.add(new ImmutablePair<String,String>(fvp.getValue(), languageCode));
            				
            				insertFacetValues(fvp.getFacetConfiguration().getName(), cfmList, cmdiData, fvp.getFacetConfiguration().getAllowMultipleValues(), fvp.getFacetConfiguration().isCaseInsensitive(), false);

            			}
            		}
            	}
            	
            	return true;
            }
            
            //end of cfm implementation

            final List<String> postProcessed = postProcess(config.getName(), value, cmdiData);
            addValuesToList(config.getName(), postProcessed, valueLangPairList, languageCode);

            String vcl = extractValueConceptLink(nav);
            if (vcl!=null) {
                ImmutablePair<String,String> vp = null;
                if (vcl.contains("CCR_")) {
                    vp = CCR.getValue(new URI(vcl));
                } else if (pattern.hasVocabulary()) {
                    vp = pattern.getVocabulary().getValue(new URI(vcl));
                }
                if (vp!=null) {
                    final String v = (String)vp.getLeft();
                    final String l = (vp.getRight()!=null?postProcessors.get(FacetConstants.FIELD_LANGUAGE_CODE).process((String)vp.getRight(),cmdiData).get(0):DEFAULT_LANGUAGE);
                    for(String pv : postProcess(config.getName(),v,cmdiData)) {
                        valueLangPairList.add(new ImmutablePair<>(pv,l));
                    }
                }
            }
            
            index = ap.evalXPath();
        }
        
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
        insertFacetValues(config.getName(), reorderedValueLangPairList, cmdiData, allowMultipleValues, config.isCaseInsensitive(), true);

        // insert post-processed values into derived facet(s) if configured
        for (FacetConfiguration derivedFacet : config.getDerivedFacets()) {
            final List<Pair<String, String>> derivedValueLangPairList = new ArrayList<>();
            for (Pair<String, String> valueLangPair : reorderedValueLangPairList) {
                for (String derivedValue : postProcess(derivedFacet.getName(), valueLangPair.getLeft(), cmdiData)) {
                    derivedValueLangPairList.add(new ImmutablePair<>(derivedValue, valueLangPair.getRight()));
                }
            }
            insertFacetValues(derivedFacet.getName(), derivedValueLangPairList, cmdiData, derivedFacet.getAllowMultipleValues(), derivedFacet.isCaseInsensitive(), false);
        }

        return matchedPattern;
    }

    private String extractLanguageCode(VTDNav nav) throws NavException {
        // extract language code in xml:lang if available
        Integer langAttrIndex = nav.getAttrVal("xml:lang");
        String languageCode;
        if (langAttrIndex != -1) {
            languageCode = nav.toString(langAttrIndex).trim();
        } else {
            return DEFAULT_LANGUAGE;
        }

        return postProcessors.get(FacetConstants.FIELD_LANGUAGE_CODE).process(languageCode, null).get(0);
    }

    private void addValuesToList(String facetName, final List<String> values, List<Pair<String, String>> valueLangPairList, final String languageCode) {
        for (String value : values) {
            // ignore non-English language names for facet LANGUAGE_CODE
            if (facetName.equals(FacetConstants.FIELD_LANGUAGE_CODE) && !languageCode.equals(ENGLISH_LANGUAGE) && !languageCode.equals(DEFAULT_LANGUAGE)) {
                continue;
            }
            valueLangPairList.add(new ImmutablePair<>(value, languageCode));
        }
    }

    private String extractValueConceptLink(VTDNav nav) throws NavException {
        // extract english for ValueConceptLink if available
        Integer vclAttrIndex = nav.getAttrVal("cmd:ValueConceptLink");
        String vcl = null;
        if (vclAttrIndex != -1) {
            vcl = nav.toString(vclAttrIndex).trim();
        }
        return vcl;
    }

    private void insertFacetValues(String name, List<Pair<String, String>> valueLangPairList, CMDIData cmdiData, boolean allowMultipleValues, boolean caseInsensitive, boolean hasPriority) {

        for (int i = 0; i < valueLangPairList.size(); i++) {
//            if (!allowMultipleValues && i > 0) {
//                break;
//            }
            String fieldValue = valueLangPairList.get(i).getLeft().trim();
            if (name.equals(FacetConstants.FIELD_DESCRIPTION)) {
                fieldValue = "{" + valueLangPairList.get(i).getRight() + "}" + fieldValue;
            }
            if(!allowMultipleValues){
            	if(hasPriority)
            		cmdiData.replaceDocField(name, fieldValue, caseInsensitive);
            	else
            		cmdiData.addDocFieldIfNull(name, fieldValue, caseInsensitive);
            	break;
            }
            cmdiData.addDocField(name, fieldValue, caseInsensitive);
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
            PostProcessor processor = postProcessors.get(facetName);
            resultList = processor.process(extractedValue, cmdiData);
        } else {
            resultList.add(extractedValue);
        }
        return resultList;
    }
}
