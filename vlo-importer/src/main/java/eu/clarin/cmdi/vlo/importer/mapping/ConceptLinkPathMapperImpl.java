package eu.clarin.cmdi.vlo.importer.mapping;

import eu.clarin.cmdi.vlo.importer.ProfileXsdWalker;
import static eu.clarin.cmdi.vlo.CmdConstants.CMD_NAMESPACE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.Vocabulary;
import java.util.HashMap;

public class ConceptLinkPathMapperImpl extends ProfileXsdWalker<Map<String, List<Pattern>>> implements ConceptLinkPathMapper {

    private final static Logger LOG = LoggerFactory.getLogger(FacetMappingFactory.class);
    private final VloConfig vloConfig;

    public ConceptLinkPathMapperImpl(VloConfig vloConfig, String xsd, Boolean useLocalXSDCache) {
        super(vloConfig, xsd, useLocalXSDCache);
        this.vloConfig = vloConfig;

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

    @Override
    protected Map<String, List<Pattern>> createResultObject() {
        return new HashMap<>();
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
    public Map<String, List<Pattern>> createConceptLinkPathMapping() throws NavException, URISyntaxException {
        return walkProfile();
    }

    @Override
    protected void processElement(VTDNav vn, LinkedList<Token> elementPath, Map<String, List<Pattern>> result) throws NavException, URISyntaxException {
        int conceptLinkIndex = getConceptLinkIndex(vn);
        if (conceptLinkIndex != -1) {
            String conceptLink = vn.toNormalizedString(conceptLinkIndex);
            Pattern xpath = createXpath(elementPath, null);

            result.computeIfAbsent(conceptLink, k -> new ArrayList<>()).add(xpath);

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

    @Override
    protected void processAttribute(VTDNav vn, LinkedList<Token> elementPath, Map<String, List<Pattern>> result) throws URISyntaxException, NavException {
        int attributeConceptLinkIndex = getConceptLinkIndex(vn);
        int attributeNameIndex = vn.getAttrVal("name");

        if (attributeNameIndex != -1 && attributeConceptLinkIndex != -1) {
            String attributeName = vn.toNormalizedString(attributeNameIndex);
            String conceptLink = vn.toNormalizedString(attributeConceptLinkIndex);

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

    /**
     * Goal is to get the "ConceptLink" attribute. Tries a number of different
     * favors that were found in the xsd's.
     *
     * @return -1 if index is not found.
     */
    private int getConceptLinkIndex(VTDNav vn) throws NavException {
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

}
