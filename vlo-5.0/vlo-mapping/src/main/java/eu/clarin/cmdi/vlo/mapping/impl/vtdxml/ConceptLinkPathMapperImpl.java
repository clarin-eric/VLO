package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.model.Context;
import eu.clarin.cmdi.vlo.mapping.model.ContextImpl;
import static eu.clarin.cmdi.vlo.util.CmdConstants.CMD_NAMESPACE;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ConceptLinkPathMapperImpl extends ProfileXsdWalker<Map<String, Context>> implements ConceptLinkPathMapper {

    private final static Logger LOG = LoggerFactory.getLogger(ConceptLinkPathMapperImpl.class);
    private final VloMappingConfiguration config;

    public ConceptLinkPathMapperImpl(VloMappingConfiguration config) {
        this(config, new DefaultVTDProfileParser(config));
    }

    public ConceptLinkPathMapperImpl(VloMappingConfiguration config, VTDProfileParser profileParser) {
        super(profileParser);
        this.config = config;

    }

    @Override
    protected Map<String, Context> createResultObject() {
        return new HashMap<>();
    }

    /**
     * "this is where the magic happens". Finds paths in the xsd to all concepts
     * (isocat data catagories).
     *
     * @param profileId Id of CMDI profile the component registry
     * @return Map (Data Category -> List of XPath expressions linked to the key
     * data category which can be found in CMDI files with this schema)
     * @throws NavException
     */
    @Override
    public Map<String, Context> createConceptLinkPathMapping(String profileId) throws NavException {
        return walkProfile(profileId);
    }

    /**
     * Process an XML element
     *
     * @param vn navigator
     * @param elementPath current tokens path
     * @param result result object
     * @throws NavException
     */
    @Override
    protected void processElement(VTDNav vn, LinkedList<Token> elementPath, Map<String, Context> result) throws NavException {
        final String xpath = createXpath(elementPath, null);
        final List<String> conceptPath = getConceptPath(vn, elementPath, result);
        final Vocabulary vocab = getVocabulary(vn);
        result.computeIfAbsent(xpath, x -> new ContextImpl(x, conceptPath, vocab));
    }

    /**
     * Process an XML attribute
     *
     * @param vn navigator
     * @param elementPath current tokens path
     * @param result result object
     * @throws NavException
     */
    @Override
    protected void processAttribute(VTDNav vn, LinkedList<Token> elementPath, Map<String, Context> result) throws NavException {
        int attributeNameIndex = vn.getAttrVal("name");

        if (attributeNameIndex != -1) {
            final String attributeName = vn.toNormalizedString(attributeNameIndex);
            final String xpath = createXpath(elementPath, attributeName);
            final List<String> conceptPath = ImmutableList.copyOf(getConceptPath(vn, elementPath, result));
            final Vocabulary vocab = getVocabulary(vn);
            result.computeIfAbsent(xpath, x -> new ContextImpl(x, conceptPath, vocab));
        }
    }

    /**
     * Creates the concept path for the current node
     *
     * @param vn navigator
     * @param elementPath current element path
     * @param result result object
     * @return list representing the concept path
     * @throws NavException
     */
    private List<String> getConceptPath(VTDNav vn, LinkedList<Token> elementPath, Map<String, Context> result) throws NavException {
        // determine the concept link
        final String conceptLink;
        final int attributeConceptLinkIndex = getConceptLinkIndex(vn);
        if (attributeConceptLinkIndex < 0) {
            conceptLink = null;
        } else {
            conceptLink = vn.toNormalizedString(attributeConceptLinkIndex);
        }
        
        // consturct the complete context path
        return constructContextPath(elementPath, result, conceptLink);
    }

    private List<String> constructContextPath(LinkedList<Token> elementPath, Map<String, Context> result, final String conceptLink) {
        // look up parent's context path
        final Context parentContext = getParentContextPath(elementPath, result);

        final LinkedList<String> conceptPath;
        if (parentContext == null) {
            // no parent context path - we start from scratch
            conceptPath = Lists.newLinkedList();
        } else {
            // we can extend the parent's context path
            conceptPath = Lists.newLinkedList(parentContext.getConceptPath());
        }
        // first element is current concept link (may be null)
        conceptPath.addFirst(conceptLink);
        return conceptPath;
    }

    private Context getParentContextPath(LinkedList<Token> elementPath, Map<String, Context> result) {
        final List<Token> parentElementPath = getParentElementPath(elementPath);
        return result.get(createXpath(parentElementPath, null));
    }

    private List<Token> getParentElementPath(List<Token> elementPath) {
        if (elementPath.size() <= 1) {
            return Collections.emptyList();
        } else {
            return elementPath.subList(0, elementPath.size() - 1);
        }
    }

    private Vocabulary getVocabulary(VTDNav vn) throws NavException {
        int vocabIndex = getVocabIndex(vn);
        if (vocabIndex < 0) {
            return null;
        } else {
            final String uri = vn.toNormalizedString(vocabIndex);
            final Vocabulary vocab = new Vocabulary(config.getVocabularyRegistryUrl(), URI.create(uri));
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
            return vocab;
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
