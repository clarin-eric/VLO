package eu.clarin.cmdi.vlo.importer.mapping;

import static eu.clarin.cmdi.vlo.CmdConstants.CMD_NAMESPACE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.Vocabulary;


public class ConceptLinkPathMapperImpl implements ConceptLinkPathMapper {
    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactory.class);
    private final VloConfig vloConfig;
    private final String xsd;
    private final Boolean useLocalXSDCache;
    
    public ConceptLinkPathMapperImpl(VloConfig vloConfig, String xsd, Boolean useLocalXSDCache) {
        this.vloConfig = vloConfig;
        this.xsd = xsd;
        this.useLocalXSDCache = useLocalXSDCache;
    }



    @Override
    public String getXsd() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean useLocalXSDCache() {
        // TODO Auto-generated method stub
        return null;
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
     * @throws URISyntaxException 
     */
    @Override
    public Map<String, List<Pattern>> createConceptLinkPathMapping() throws NavException, URISyntaxException{
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
                    
                    result.computeIfAbsent(conceptLink, k -> new ArrayList<Pattern>()).add(xpath);

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
                            
                            result.computeIfAbsent(conceptLink, k -> new ArrayList<Pattern>()).add(xpath);

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

}
