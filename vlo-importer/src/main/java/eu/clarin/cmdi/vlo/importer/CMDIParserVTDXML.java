package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMDIParserVTDXML implements CMDIDataProcessor {

    private final Map<String, PostProcessor> postProcessors;
    private final Boolean useLocalXSDCache;
    private static final Pattern PROFILE_ID_PATTERN = Pattern.compile(".*(clarin.eu:cr1:p_[0-9]+).*");
    private final static Logger LOG = LoggerFactory.getLogger(CMDIParserVTDXML.class);

    private static final String DEFAULT_LANGUAGE = "code:und";
    private final VloConfig config;

    public CMDIParserVTDXML(Map<String, PostProcessor> postProcessors, VloConfig config, Boolean useLocalXSDCache) {
        this.postProcessors = postProcessors;
        this.useLocalXSDCache = useLocalXSDCache;
        this.config = config;
    }

    @Override
    public CMDIData process(File file) throws VTDException, IOException {
        final CMDIData cmdiData = new CMDIData();
        final VTDGen vg = new VTDGen();
        final FileInputStream fileInputStream = new FileInputStream(file);
        try {
            vg.setDoc(IOUtils.toByteArray(fileInputStream));
            vg.parse(true);
        } finally {
            fileInputStream.close();
        }

        final VTDNav nav = vg.getNav();
        final FacetMapping facetMapping = getFacetMapping(nav.cloneNav());

        if (facetMapping.getFacets().isEmpty()) {
            LOG.error("Problems mapping facets for file: {}", file.getAbsolutePath());
        }

        nav.toElement(VTDNav.ROOT);
        processResources(cmdiData, nav);
        processFacets(cmdiData, nav, facetMapping);
        return cmdiData;
    }

    @Override
    public String extractMdSelfLink(File file) throws VTDException, IOException {
        final VTDGen vg = new VTDGen();
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            vg.setDoc(IOUtils.toByteArray(fileInputStream));
            vg.parse(true);
        }
        final VTDNav nav = vg.getNav();
        nav.toElement(VTDNav.ROOT);
        AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, null);
        ap.selectXPath("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()");
        int index = ap.evalXPath();

        String mdSelfLink = null;
        if (index != -1) {
            mdSelfLink = nav.toString(index).trim();
        }
        return mdSelfLink;
    }

    /**
     * Setting namespace for Autopilot ap
     *
     * @param ap
     */
    private void setNameSpace(AutoPilot ap, String profileId) {
        ap.declareXPathNameSpace("cmd", "http://www.clarin.eu/cmd/1");
        if(profileId != null) {
            ap.declareXPathNameSpace("cmdp", "http://www.clarin.eu/cmd/1/profiles/"+profileId);
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
        String profileId = extractXsd(nav);
        if (profileId == null) {
            throw new RuntimeException("Cannot get xsd schema so cannot get a proper mapping. Parse failed!");
        }
//        final VloConfig config = MetadataImporter.config;
//        final URI facetConceptsFile
//                = FacetConceptsMarshaller.resolveFacetsFile(config.getConfigLocation(), config.getFacetConceptsFile());
//        final String facetConceptsFilePath = new File(facetConceptsFile).getAbsolutePath();
//        return FacetMappingFactory.getFacetMapping(facetConceptsFilePath, profileId, useLocalXSDCache);

        String facetConceptsFile = MetadataImporter.config.getFacetConceptsFile();

        //resolve against config location? (empty = default location)
        if (facetConceptsFile != null && !facetConceptsFile.isEmpty()) {
            URI configLocation = MetadataImporter.config.getConfigLocation();
            if (configLocation != null && !configLocation.getScheme().equals("jar")) {
                URI facetConceptsLocation = configLocation.resolve(facetConceptsFile);
                facetConceptsFile = new File(facetConceptsLocation).getAbsolutePath();
            }
        }

        return FacetMappingFactory.getFacetMapping(facetConceptsFile, profileId, useLocalXSDCache);
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
            result = schemaLocationArray[schemaLocationArray.length-1];
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
    private void processResources(CMDIData cmdiData, VTDNav nav) throws VTDException {
        AutoPilot mdSelfLink = new AutoPilot(nav);
        setNameSpace(mdSelfLink, null);
        mdSelfLink.selectXPath("/cmd:CMD/cmd:Header/cmd:MdSelfLink");
        String mdSelfLinkString = mdSelfLink.evalXPathToString();
        if (config.isProcessHierarchies()) {
            ResourceStructureGraph.addResource(mdSelfLinkString);
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
            if (config.isProcessHierarchies() && type.toLowerCase().equals("metadata")) {
                ResourceStructureGraph.addEdge(ref, mdSelfLinkString);
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
    private void processFacets(CMDIData cmdiData, VTDNav nav, FacetMapping facetMapping) throws VTDException {
        List<FacetConfiguration> facetList = facetMapping.getFacets();
        for (FacetConfiguration config : facetList) {
            boolean matchedPattern = false;
            List<String> patterns = config.getPatterns();
            for (String pattern : patterns) {
                matchedPattern = matchPattern(cmdiData, nav, config, pattern, config.getAllowMultipleValues());
                if (matchedPattern && !config.getAllowMultipleValues()) {
                    break;
                }
            }

            // using fallback patterns if extraction failed
            if (matchedPattern == false) {
                for (String pattern : config.getFallbackPatterns()) {
                    matchedPattern = matchPattern(cmdiData, nav, config, pattern, config.getAllowMultipleValues());
                    if (matchedPattern && !config.getAllowMultipleValues()) {
                        break;
                    }
                }
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
    private boolean matchPattern(CMDIData cmdiData, VTDNav nav, FacetConfiguration config, String pattern, Boolean allowMultipleValues) throws VTDException {
        final AutoPilot ap = new AutoPilot(nav);
        setNameSpace(ap, extractXsd(nav));
        ap.selectXPath(pattern);

        boolean matchedPattern = false;
        int index = ap.evalXPath();
        while (index != -1) {
            matchedPattern = true;
            if (nav.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
                //if it is an attribute you need to add 1 to the index to get the right value
                index++;
            }
            final String value = nav.toString(index);

            final String languageCode = extractLanguageCode(nav);

            // ignore non-English language names for facet LANGUAGE_CODE
            if (config.getName().equals(FacetConstants.FIELD_LANGUAGE_CODE) && !languageCode.equals("code:eng") && !languageCode.equals("code:und")) {
                index = ap.evalXPath();
                continue;
            }

            final List<String> values = postProcess(config.getName(), value);
            //discard '--' values
            if (values != null && !values.isEmpty() && values.get(0).equals("--")) {
                return matchedPattern;
            }

            insertFacetValues(config.getName(), values, cmdiData, languageCode, allowMultipleValues, config.isCaseInsensitive());

            // insert post-processed values into derived facet(s) if configured
            for (String derivedFacet : config.getDerivedFacets()) {
                final List<String> derivedValues = new ArrayList<String>();
                for (String postProcessedValue : values) {
                    derivedValues.addAll(postProcess(derivedFacet, postProcessedValue));
                }
                insertFacetValues(derivedFacet, derivedValues, cmdiData, languageCode, allowMultipleValues, config.isCaseInsensitive());
            }

            index = ap.evalXPath();

            if (!allowMultipleValues) {
                break;
            }
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

        return postProcessors.get(FacetConstants.FIELD_LANGUAGE_CODE).process(languageCode).get(0);
    }

    private void insertFacetValues(String name, List<String> valueList, CMDIData cmdiData, String languageCode, boolean allowMultipleValues, boolean caseInsensitive) {
        for (int i = 0; i < valueList.size(); i++) {
            if (!allowMultipleValues && i > 0) {
                break;
            }
            String fieldValue = valueList.get(i).trim();
            if (name.equals(FacetConstants.FIELD_DESCRIPTION)) {
                fieldValue = "{" + languageCode + "}" + fieldValue;
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
    private List<String> postProcess(String facetName, String extractedValue) {
        List<String> resultList = new ArrayList<String>();
        if (postProcessors.containsKey(facetName)) {
            PostProcessor processor = postProcessors.get(facetName);
            resultList = processor.process(extractedValue);
        } else {
            resultList.add(extractedValue);
        }
        return resultList;
    }
}
