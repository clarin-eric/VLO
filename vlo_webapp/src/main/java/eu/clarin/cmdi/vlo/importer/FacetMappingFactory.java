package eu.clarin.cmdi.vlo.importer;

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

import eu.clarin.cmdi.vlo.importer.FacetConceptMapping.FacetConcept;

public class FacetMappingFactory {

    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactory.class);

    private Map<String, FacetMapping> mapping = new HashMap<String, FacetMapping>();

    private final static FacetMappingFactory INSTANCE = new FacetMappingFactory();

    private FacetMappingFactory() {
    }

    public static FacetMapping getFacetMapping(String xsd) {
        return INSTANCE.getOrCreateMapping(xsd);
    }

    private FacetMapping getOrCreateMapping(String xsd) {
        FacetMapping result = mapping.get(xsd);
        if (result == null) {
            result = createMapping(xsd);
            mapping.put(xsd, result);
        }
        return result;
    }

    private FacetMapping createMapping(String xsd) {
        FacetMapping result = new FacetMapping();
        addDefaults(result);
        FacetConceptMapping conceptMapping = VLOMarshaller.getFacetConceptMapping();
        try {
            VTDGen vg = new VTDGen();
            vg.parseHttpUrl(xsd, true);
            VTDNav vn = vg.getNav();
            for (FacetConcept facetConcept : conceptMapping.getFacetConcepts()) {
                FacetConfiguration config = new FacetConfiguration();
                List<String> xpaths = new ArrayList<String>();
                List<String> concepts = facetConcept.getConcepts();
                List<String> xpathsFound = createXpathFromConceptLink(concepts, vn);
                vn.toElement(VTDNav.ROOT);
                xpaths.addAll(xpathsFound);
                if (xpathsFound.isEmpty()) {
                    //add hardcoded patterns only when there is no xpath generated from conceptlink
                    xpaths.addAll(facetConcept.getPatterns());
                }
                config.setCaseInsensitive(facetConcept.isCaseInsensitive());
                config.setPatterns(xpaths);
                config.setName(facetConcept.getName());
                if (!config.getPatterns().isEmpty()) {
                    result.addFacet(config);
                }
            }
        } catch (NavException e) {
            LOG.error("Error creating facetMapping from xsd: " + xsd + " ", e);
        }
        return result;
    }

    private void addDefaults(FacetMapping result) {
        result.setIdMapping("/CMD/Header/MdSelfLink/text()");
    }

    private List<String> createXpathFromConceptLink(List<String> concepts, VTDNav vn) throws NavException {
        List<String> result = new ArrayList<String>();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectElement("xs:element");
        Deque<Token> elementPath = new LinkedList<Token>();
        while (ap.iterate()) {
            int i = vn.getAttrVal("name");
            if (i != -1) {
                String elementName = vn.toNormalizedString(i);
                updateElementPath(vn, elementPath, elementName);
                int t = vn.getAttrValNS("http://www.isocat.org/ns/dcr", "datcat");
                if (t != -1) {
                    String dataCategoryValue = vn.toNormalizedString(t);
                    for (String conceptLink : concepts) {
                        if (dataCategoryValue.equalsIgnoreCase(conceptLink)) {
                            String xpath = "/";
                            for (Token token : elementPath) {
                                xpath += token.name + "/";
                            }
                            result.add(xpath + "text()");
                        }
                    }
                }
            }
        }
        return result;
    }

    private void updateElementPath(VTDNav vn, Deque<Token> elementPath, String elementName) throws NavException {
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
