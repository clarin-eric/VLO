package eu.clarin.cmdi.vlo.importer.mapping;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import static eu.clarin.cmdi.vlo.CmdConstants.CMD_NAMESPACE;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.Vocabulary;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.AcceptableContext;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.FacetConcept;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.RejectableContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Creates facet-mappings (xpaths) from a configuration. As they say "this is
 * where the magic happens". Also does some caching.
 */
public class FacetMappingFactory {

    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactory.class);

    private final ConcurrentHashMap<String, FacetMapping> mapping = new ConcurrentHashMap<>();

    /**
     * Our one instance of the FMF.
     */
    private final VloConfig vloConfig;

    private final FieldNameServiceImpl fieldNameService;

    private final FacetConceptMapping conceptMapping;

    private final Map<String, ConditionTargetSet> conditionTargetSetPerFacet;

    public FacetMappingFactory(VloConfig vloConfig, VLOMarshaller marshaller) {
        this.vloConfig = vloConfig;
        this.fieldNameService = new FieldNameServiceImpl(vloConfig);
        //this.marshaller = marshaller;

        this.conceptMapping = marshaller.getFacetConceptMapping(vloConfig.getFacetConceptsFile());

        this.conditionTargetSetPerFacet = new ValueMappingFactoryDOMImpl().getValueMappings(vloConfig.getValueMappingsFile(), this.conceptMapping);
    }

    /**
     *
     * @param facetConceptsFile path to facet concepts file, leave null or empty
     * to use default
     * @param xsd
     * @param useLocalXSDCache
     * @return
     */
    public FacetMapping getFacetMapping(String xsd, Boolean useLocalXSDCache) {
        return getOrCreateMapping(xsd, useLocalXSDCache);
    }

    /**
     * Get facet concept mapping.
     *
     * Get facet mapping used to map meta data based on a facet concepts file
     * and url to cmdi meta data profile.
     *
     * @param facetConcepts name of the facet concepts file
     * @param xsd url of xml schema of cmdi profile
     * @param useLocalXSDCache use local XML schema files instead of accessing
     * the component registry
     *
     * @return facet concept mapping
     */
    private FacetMapping getOrCreateMapping(String xsd, Boolean useLocalXSDCache) {
        return mapping.computeIfAbsent(xsd, (key) -> {
            return createMapping(xsd, useLocalXSDCache);
        });
    }

    /**
     * Create facet concept mapping.
     *
     * Create facet mapping used to map meta data based on a facet concept
     * mapping file and url to cmdi meta data profile.
     *
     * @param facetConcepts name of the facet concepts file
     * @param xsd url of xml schema of cmdi profile
     * @param useLocalXSDCache use local XML schema files instead of accessing
     * the component registry
     *
     * @return the facet mapping used to map meta data to facets
     */
    private FacetMapping createMapping(String xsd, Boolean useLocalXSDCache) {
        LOG.debug("Creating mapping for {} using {} (useLocalXSDCache: {})", xsd, this.vloConfig.getFacetConceptsFile(), useLocalXSDCache);
        FacetMapping facetMapping = new FacetMapping();
        // Gets the configuration. VLOMarshaller only reads in the facetconceptmapping.xml file and returns the facetMapping (though the reading in is implicit).
//        FacetConceptMapping conceptMapping = marshaller.getFacetConceptMapping(facetConcepts);
        try {
            //The magic
            Map<String, List<Pattern>> conceptLinkPathMapping = createConceptLinkPathMapping(xsd, useLocalXSDCache);
            Map<Pattern, String> pathConceptLinkMapping = null;
            // Below we put the stuff we found into the configuration class.
            for (FacetConcept facetConcept : this.conceptMapping.getFacetConcepts()) {
                LOG.trace("-- Facet concept {}", facetConcept);
                FacetConfiguration config = facetMapping.getFacetConfiguration(facetConcept.getName());
                List<Pattern> xpaths = new ArrayList<>();
                handleId(xpaths, facetConcept);
                for (String concept : facetConcept.getConcepts()) {
                    LOG.trace("-- -- Concept {}", concept);
                    List<Pattern> paths = conceptLinkPathMapping.get(concept);
                    if (paths != null) {
                        if (facetConcept.hasContext()) {
                            LOG.trace("-- -- -- Concept has context");
                            for (Pattern path : paths) {
                                LOG.trace("-- -- -- Concept path {}", path);
                                // lazily instantiate the reverse mapping, i.e., from path to concept
                                if (pathConceptLinkMapping == null) {
                                    pathConceptLinkMapping = new HashMap<>();
                                    for (String c : conceptLinkPathMapping.keySet()) {
                                        for (Pattern p : conceptLinkPathMapping.get(c)) {
                                            pathConceptLinkMapping.put(p, c);
                                        }
                                    }
                                }
                                String context = getContext(path, pathConceptLinkMapping);
                                boolean handled = false;
                                // check against acceptable context
                                if (facetConcept.hasAcceptableContext()) {
                                    AcceptableContext acceptableContext = facetConcept.getAcceptableContext();
                                    if (context == null && acceptableContext.includeEmpty()) {
                                        // no context is accepted
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](empty) is accepted", facetConcept.getName(), path, context);
                                        xpaths.add(path);
                                        handled = true;
                                    } else if (acceptableContext.getConcepts().contains(context)) {
                                        // a specific context is accepted
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}] is accepted", facetConcept.getName(), path, context);
                                        xpaths.add(path);
                                        handled = true;
                                    }
                                }
                                // check against rejectable context
                                if (!handled && facetConcept.hasRejectableContext()) {
                                    RejectableContext rejectableContext = facetConcept.getRejectableContext();
                                    if (context == null && rejectableContext.includeEmpty()) {
                                        // no context is rejected
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](empty) is rejected", facetConcept.getName(), path, context);
                                        handled = true;
                                    } else if (rejectableContext.getConcepts().contains(context)) {
                                        // a specific context is rejected
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}] is rejected", facetConcept.getName(), path, context);
                                        handled = true;
                                    } else if (rejectableContext.includeAny()) {
                                        // any context is rejected
                                        LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](any) is rejected", facetConcept.getName(), path, context);
                                        handled = true;
                                    }
                                }
                                if (!handled && context != null && facetConcept.hasAcceptableContext() && facetConcept.getAcceptableContext().includeAny()) {
                                    // any, not rejected context, is accepted
                                    LOG.trace("-- -- -- -- facet[{}] path[{}] context[{}](any) is accepted", facetConcept.getName(), path, context);
                                    xpaths.add(path);
                                }
                            }
                        } else {
                            LOG.trace("-- -- -- Paths: {} (concept has no context)", paths);
                            xpaths.addAll(paths);
                        }
                    }
                }

                // pattern-based blacklisting: remove all XPath expressions that contain a blacklisted substring;
                // this is basically a hack to enhance the quality of the visualised information in the VLO;
                // should be replaced by a more intelligent approach in the future
                for (Pattern blacklistPattern : facetConcept.getBlacklistPatterns()) {
                    Iterator<Pattern> xpathIterator = xpaths.iterator();
                    while (xpathIterator.hasNext()) {
                        Pattern xpath = xpathIterator.next();
                        if (xpath.getPattern().contains(blacklistPattern.getPattern())) {
                            LOG.debug("Rejecting {} because of blacklisted substring {}", xpath, blacklistPattern);
                            xpathIterator.remove();
                        }
                    }
                }

                config.setCaseInsensitive(facetConcept.isCaseInsensitive());
                config.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
                config.setMultilingual(facetConcept.isMultilingual());
//                config.setName(facetConcept.getName());

                LinkedHashSet<Pattern> linkedHashSet = new LinkedHashSet<>(xpaths);
                if (xpaths.size() != linkedHashSet.size()) {
                    LOG.error("Duplicate XPaths for facet {} in: {}.", facetConcept.getName(), xpaths);
                }
                config.setPatterns(new ArrayList<>(linkedHashSet));
                config.setFallbackPatterns(facetConcept.getPatterns());

                //set derived facets
                for (String derivedFacetName : facetConcept.getDerivedFacets()) {

                    config.addDerivedFacet(facetMapping.getFacetConfiguration(derivedFacetName));
                }

                // setValueMappings
                if (this.conditionTargetSetPerFacet.containsKey(config.getName())) {
                    config.setConditionTargetSet(this.conditionTargetSetPerFacet.get(config.getName()));
                }
            }

            //now where all FacetConfigurations are created we can build references for derived facets
        } catch (NavException | URISyntaxException e) {
            LOG.error("Error creating facetMapping from xsd: {}", xsd, e);
        }
        LOG.debug("Mapping for {}: {}", xsd, facetMapping);
        return facetMapping;
    }

    /**
     * Look if there is a contextual (container) data category associated with
     * an ancestor by walking back.
     */
    private String getContext(Pattern path, Map<Pattern, String> pathConceptLinkMapping) {
        String context = null;
        String cpath = path.getPattern();
        while (context == null && !cpath.equals("/text()")) {
            if (cpath.contains("@")) {
                // go to the parent element of the attribute
                cpath = cpath.replaceAll("/@.*", "/text()");
            } else {
                // go to the parent element of the element
                cpath = cpath.replaceAll("/[^/]*/text\\(\\)", "/text()");
            }
            context = pathConceptLinkMapping.get(new Pattern(cpath));
        }
        return context;
    }

    /**
     * The id facet is special case and patterns must be added first. The
     * standard pattern to get the id out of the header is the most reliable and
     * it should fall back on concept matching if nothing matches. (Note this is
     * the exact opposite of other facets where the concept match is probably
     * better then the 'hardcoded' pattern).
     */
    private void handleId(List<Pattern> xpaths, FacetConcept facetConcept) {
        if (fieldNameService.getFieldName(FieldKey.ID).equals(facetConcept.getName())) {
            xpaths.addAll(facetConcept.getPatterns());
        }
    }

    /**
     * "this is where the magic happens". Finds paths in the xsd to all concepts
     * (isocat data catagories).
     *
     * @param xsd URL of XML Schema of some CMDI profile
     * @param useLocalXSDCache use local XML schema files instead of accessing
     * the component registry
     * @return Map (Data Category -> List of XPath expressions linked to the key
     * data category which can be found in CMDI files with this schema)
     * @throws NavException
     */
    private Map<String, List<Pattern>> createConceptLinkPathMapping(String xsd, Boolean useLocalXSDCache) throws NavException, URISyntaxException {
        Map<String, List<Pattern>> result = new HashMap<>();
        VTDGen vg = new VTDGen();
        boolean parseSuccess;
        if (useLocalXSDCache) {
            parseSuccess = vg.parseFile(Thread.currentThread().getContextClassLoader().getResource("testProfiles/" + xsd + ".xsd").getPath(), true);
        } else {
            parseSuccess = vg.parseHttpUrl(vloConfig.getComponentRegistryProfileSchema(xsd), true);
        }

        if (!parseSuccess) {
            LOG.error("Cannot create ConceptLink Map from xsd (xsd is probably not reachable): " + xsd + ". All metadata instances that use this xsd will not be imported correctly.");
            return result; //return empty map, so the incorrect xsd is not tried for all metadata instances that specify it.
        }
        VTDNav vn = vg.getNav();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectElement("xs:element");
        Deque<Token> elementPath = new LinkedList<>();
        while (ap.iterate()) {
            int i = vn.getAttrVal("name");
            if (i != -1) {
                String elementName = vn.toNormalizedString(i);
                updateElementPath(vn, elementPath, elementName);
                int datcatIndex = getDatcatIndex(vn);
                if (datcatIndex != -1) {
                    String conceptLink = vn.toNormalizedString(datcatIndex);
                    Pattern xpath = createXpath(elementPath, null);
                    List<Pattern> paths = result.get(conceptLink);
                    if (paths == null) {
                        paths = new ArrayList<>();
                        result.put(conceptLink, paths);
                    }
                    paths.add(xpath);
                    int vocabIndex = getVocabIndex(vn);
                    if (vocabIndex != -1) {
                        String uri = vn.toNormalizedString(vocabIndex);
                        Vocabulary vocab = new Vocabulary(vloConfig.getVocabularyRegistryUrl(), new URI(uri));
                        xpath.setVocabulary(vocab);
                        int propIndex = getVocabPropIndex(vn);
                        if (propIndex != -1) {
                            String prop = vn.toNormalizedString(propIndex);
                            vocab.setProperty(prop);
                        }
                        int langIndex = getVocabLangIndex(vn);
                        if (langIndex != -1) {
                            String lang = vn.toNormalizedString(langIndex);
                            vocab.setLanguage(lang);
                        }
                    }
                }

                // look for associated attributes with concept links
                vn.push();
                AutoPilot attributeAutopilot = new AutoPilot(vn);
                attributeAutopilot.declareXPathNameSpace("xs", "http://www.w3.org/2001/XMLSchema");

                try {
                    attributeAutopilot.selectXPath("./xs:complexType/xs:simpleContent/xs:extension/xs:attribute | ./xs:complexType/xs:attribute");
                    while (attributeAutopilot.evalXPath() != -1) {
                        int attributeDatcatIndex = getDatcatIndex(vn);
                        int attributeNameIndex = vn.getAttrVal("name");

                        if (attributeNameIndex != -1 && attributeDatcatIndex != -1) {
                            String attributeName = vn.toNormalizedString(attributeNameIndex);
                            String conceptLink = vn.toNormalizedString(attributeDatcatIndex);

                            Pattern xpath = createXpath(elementPath, attributeName);
                            List<Pattern> values = result.get(conceptLink);
                            if (values == null) {
                                values = new ArrayList<>();
                                result.put(conceptLink, values);
                            }
                            values.add(xpath);
                            int vocabIndex = getVocabIndex(vn);
                            if (vocabIndex != -1) {
                                String uri = vn.toNormalizedString(vocabIndex);
                                Vocabulary vocab = new Vocabulary(vloConfig.getVocabularyRegistryUrl(), new URI(uri));
                                xpath.setVocabulary(vocab);
                                int propIndex = getVocabPropIndex(vn);
                                if (propIndex != -1) {
                                    String prop = vn.toNormalizedString(propIndex);
                                    vocab.setProperty(prop);
                                }
                                int langIndex = getVocabLangIndex(vn);
                                if (langIndex != -1) {
                                    String lang = vn.toNormalizedString(langIndex);
                                    vocab.setLanguage(lang);
                                }
                            }
                        }
                    }
                } catch (XPathParseException | XPathEvalException | NavException e) {
                    LOG.error("Cannot extract attributes for element " + elementName + ". Will continue anyway...", e);
                }

                // returning to normal element-based workflow
                vn.pop();
            }
        }
        return result;
    }

    /**
     * Goal is to get the "datcat" attribute. Tries a number of different favors
     * that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getDatcatIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "ConceptLink");
        if (result == -1) {
            result = vn.getAttrVal("cmd:ConceptLink");
        }
        return result;
    }

    /**
     * Goal is to get the "Vocabulary URI" attribute. Tries a number of
     * different favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getVocabIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "Vocabulary");
        if (result == -1) {
            result = vn.getAttrVal("cmd:Vocabulary");
        }
        return result;
    }

    /**
     * Goal is to get the "Vocabulary Property" attribute. Tries a number of
     * different favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getVocabPropIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "ValueProperty");
        if (result == -1) {
            result = vn.getAttrVal("cmd:ValueProperty");
        }
        return result;
    }

    /**
     * Goal is to get the "Vocabulary Language" attribute. Tries a number of
     * different favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getVocabLangIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS(CMD_NAMESPACE, "ValueLanguage");
        if (result == -1) {
            result = vn.getAttrVal("cmd:ValueLanguage");
        }
        return result;
    }

    /**
     * Given an xml-token path thingy create an xpath.
     *
     * @param elementPath
     * @param attributeName will be appended as attribute to XPath expression if
     * not null
     * @return
     */
    private Pattern createXpath(Deque<Token> elementPath, String attributeName) {
        StringBuilder xpath = new StringBuilder("/cmd:CMD/cmd:Components/");
        for (Token token : elementPath) {
            xpath.append("cmdp:").append(token.name).append("/");
        }

        if (attributeName != null) {
            return new Pattern(xpath.append("@").append(attributeName).toString());
        } else {
            return new Pattern(xpath.append("text()").toString());
        }
    }

    /**
     * does some updating after a step. To keep the path proper and path-y.
     *
     * @param vn
     * @param elementPath
     * @param elementName
     */
    private void updateElementPath(VTDNav vn, Deque<Token> elementPath, String elementName) {
        int previousDepth = elementPath.isEmpty() ? -1 : elementPath.peekLast().depth;
        int currentDepth = vn.getCurrentDepth();
        if (currentDepth == previousDepth) {
            elementPath.removeLast();
        } else if (currentDepth < previousDepth) {
            while (currentDepth <= previousDepth) {
                elementPath.removeLast();
                previousDepth = elementPath.peekLast().depth;
            }
        }
        elementPath.offerLast(new Token(currentDepth, elementName));
    }

    class Token {

        final String name;
        final int depth;

        public Token(int depth, String name) {
            this.depth = depth;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + ":" + depth;
        }
    }

    public void printMapping(File file) throws IOException {
        Set<String> xsdNames = mapping.keySet();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append("This file is generated on " + DateFormat.getDateTimeInstance().format(new Date())
                + " and only used to document the mapping.\n");
        fileWriter.append("This file contains xsd name and a list of conceptName with xpath mappings that are generated.\n");
        fileWriter.append("---------------------\n");
        fileWriter.flush();
        for (String xsd : xsdNames) {
            FacetMapping facetMapping = mapping.get(xsd);
            fileWriter.append(xsd + "\n");
            for (FacetConfiguration config : facetMapping.getFacets()) {
                fileWriter.append("FacetName:" + config.getName() + "\n");
                fileWriter.append("Mappings:\n");
                for (Pattern pattern : config.getPatterns()) {
                    fileWriter.append("    " + pattern + "\n");
                }
            }
            fileWriter.append("---------------------\n");
            fileWriter.flush();
        }
        fileWriter.close();
    }
}
