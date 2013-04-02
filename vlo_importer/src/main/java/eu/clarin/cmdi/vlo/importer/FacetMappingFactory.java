package eu.clarin.cmdi.vlo.importer;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.importer.FacetConceptMapping.FacetConcept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

/**
 * Creates facet-mappings (xpaths) from a configuration.
 * As they say "this is where the magic happens".
 * Also does some cashing.
 */

public class FacetMappingFactory {

    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactory.class);

    private Map<String, FacetMapping> mapping = new HashMap<String, FacetMapping>();

    /**
     * Our one instance of the FMF.
     */
    private final static FacetMappingFactory INSTANCE = new FacetMappingFactory();

    private FacetMappingFactory() {
    }

    public static FacetMapping getFacetMapping(String xsd) {
        return INSTANCE.getOrCreateMapping(xsd);
    }

    /**
     * If cashed gives that result, otherwise makes a new one (in the createMapping method below)..
     * @param xsd
     * @return
     */
    private FacetMapping getOrCreateMapping(String xsd) {
        FacetMapping result = mapping.get(xsd);
        if (result == null) {
            result = createMapping(xsd);
            mapping.put(xsd, result);
        }
        return result;
    }

    /**
     * Asks conceptLinkPathMapping to create the actual xpaths.
     * Does a bunch of bookkeeping in order to get the FacetMapping.
     * @param xsd
     * @return
     */
    private FacetMapping createMapping(String xsd) {
        FacetMapping result = new FacetMapping();
        // Gets the configuration. VLOMarchaller only reads in the facetconceptmapping.xml file and returns the result (though the reading in is implicit).
        FacetConceptMapping conceptMapping = VLOMarshaller.getFacetConceptMapping();
        try {
            //The magic
            Map<String, List<String>> conceptLinkPathMapping = createConceptLinkPathMapping(xsd);
            // Below we put the stuff we found into the configuration class.
            for (FacetConcept facetConcept : conceptMapping.getFacetConcepts()) {
                FacetConfiguration config = new FacetConfiguration();
                List<String> xpaths = new ArrayList<String>();
                handleId(xpaths, facetConcept);
                for (String concept : facetConcept.getConcepts()) {
                    List<String> paths = conceptLinkPathMapping.get(concept);
                    if (paths != null) {
                        xpaths.addAll(paths);
                    }
                }
                if (xpaths.isEmpty()) {
                    //add hardcoded patterns only when there is no xpath generated from conceptlink
                    xpaths.addAll(facetConcept.getPatterns());
                }
                config.setCaseInsensitive(facetConcept.isCaseInsensitive());
                config.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
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

    /**
     * The id facet is special case and patterns must be added first.
     * The standard pattern to get the id out of the header is the most reliable and it should fall back on concept matching if nothing matches.
     * (Note this is the exact opposite of other facets where the concept match is probably better then the 'hardcoded' pattern).
     */
    private void handleId(List<String> xpaths, FacetConcept facetConcept) {
        if (FacetConstants.FIELD_ID.equals(facetConcept.getName())) {
            xpaths.addAll(facetConcept.getPatterns());
        }
    }

    /**
     * "this is where the magic happens".
     * Finds paths in the xsd to all concepts (isocat data catagories).
     * @param xsd
     * @return
     * @throws NavException
     */
    private Map<String, List<String>> createConceptLinkPathMapping(String xsd) throws NavException {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        VTDGen vg = new VTDGen();
        boolean parseSuccess = vg.parseHttpUrl(xsd, true);
        if (!parseSuccess) {
            LOG.error("Cannot create ConceptLink Map from xsd (xsd is probably not reachable): "+xsd+". All metadata instances that use this xsd will not be imported correctly.");
            return result; //return empty map, so the incorrect xsd is not tried for all metadata instances that specify it.
        }
        VTDNav vn = vg.getNav();
        AutoPilot ap = new AutoPilot(vn);
        ap.selectElement("xs:element");
        Deque<Token> elementPath = new LinkedList<Token>();
        while (ap.iterate()) {
            int i = vn.getAttrVal("name");
            if (i != -1) {
                String elementName = vn.toNormalizedString(i);
                updateElementPath(vn, elementPath, elementName);
                int datcatIndex = getDatcatIndex(vn);
                if (datcatIndex != -1) {
                    String conceptLink = vn.toNormalizedString(datcatIndex);
                    String xpath = createXpath(elementPath);
                    List<String> values = result.get(conceptLink);
                    if (values == null) {
                        values = new ArrayList<String>();
                        result.put(conceptLink, values);
                    }
                    values.add(xpath);
                }
            }
        }
        return result;
    }

    /**
     * Goal is to get the "datcat" attribute. Tries a number of different favors that were found in the xsd's.
     * @return -1 if index is not found.
     */
    private int getDatcatIndex(VTDNav vn) throws NavException {
        int result = -1;
        result = vn.getAttrValNS("http://www.isocat.org/ns/dcr", "datcat");
        if (result == -1) {
            result = vn.getAttrValNS("http://www.isocat.org", "datcat");
        }
        if (result == -1) {
            result = vn.getAttrVal("dcr:datcat");
        }
        return result;
    }

    /**
     * Given an xml-token path thingy create an xpath.
     * @param elementPath
     * @return
     */
    private String createXpath(Deque<Token> elementPath) {
        StringBuilder xpath = new StringBuilder("/");
        for (Token token : elementPath) {
            xpath.append("c:").append(token.name).append("/");
        }
        return xpath.append("text()").toString();
    }

    /**
     * does some updating after a step. To keep the path proper and path-y.
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

    public static void printMapping(File file) throws IOException {
        Set<String> xsdNames = INSTANCE.mapping.keySet();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append("This file is generated on " + DateFormat.getDateTimeInstance().format(new Date())
                + " and only used to document the mapping.\n");
        fileWriter.append("This file contains xsd name and a list of conceptName with xpath mappings that are generated.\n");
        fileWriter.append("---------------------\n");
        fileWriter.flush();
        for (String xsd : xsdNames) {
            FacetMapping facetMapping = INSTANCE.mapping.get(xsd);
            fileWriter.append(xsd + "\n");
            for (FacetConfiguration config : facetMapping.getFacets()) {
                fileWriter.append("FacetName:" + config.getName() + "\n");
                fileWriter.append("Mappings:\n");
                for (String pattern : config.getPatterns()) {
                    fileWriter.append("    " + pattern + "\n");
                }
            }
            fileWriter.append("---------------------\n");
            fileWriter.flush();
        }
        fileWriter.close();
    }
}
