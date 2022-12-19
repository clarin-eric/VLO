package eu.clarin.cmdi.vlo.mapping.impl.vtdxml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.LinkedList;
import java.util.Map;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.vlo.mapping.VloMappingConfiguration;
import eu.clarin.cmdi.vlo.mapping.impl.vtdxml.ProfileXsdWalker.Token;
import eu.clarin.cmdi.vlo.mapping.impl.vtdxml.ProfileXsdWalker.VTDNavProcessor;
import eu.clarin.cmdi.vlo.mapping.model.Context;
import eu.clarin.cmdi.vlo.mapping.model.ContextImpl;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProfileContextMapFactoryImpl implements ProfileContextMapFactory {

    private final ProfileXsdWalker<Map<String, Context>> xsdWalker;

    /**
     * Constructs a context map factory with the default profile parser
     * ({@link DefaultVTDProfileParser})
     *
     * @param config mapping configuration
     */
    public ProfileContextMapFactoryImpl(VloMappingConfiguration config) {
        //TODO: use a caching parser by default
        this(config, new DefaultVTDProfileParser(config));
    }

    /**
     * Constructs a context map factory with the specified profile parser
     * (useful for loading profiles from cache or resources)
     *
     * @param config mapping configuration
     * @param profileParser parser service to use
     */
    public ProfileContextMapFactoryImpl(VloMappingConfiguration config, VTDProfileParser profileParser) {
        // walker traverses the profile XSD; handling of elements and attributes
        // happens in processors that we pass via the constructor
        this.xsdWalker = new ProfileXsdWalker<>(
                profileParser, // walker uses the provided parser
                Maps::newHashMap, // results are stored in a hashmap 
                new ElementProcessor(config), // element processor defined below
                new AttributeProcessor(config)); // attribute processor defined below
    }

    /**
     * Processes an XML element
     */
    protected class ElementProcessor extends VTDProfileProcessor implements VTDNavProcessor<Map<String, Context>> {

        public ElementProcessor(VloMappingConfiguration config) {
            super(config);
        }

        @Override
        public void process(VTDNav vn, LinkedList<Token> elementPath, Map<String, Context> result) throws NavException {
            final String xpath = createXpath(elementPath, null);
            final List<String> conceptPath = getConceptPath(vn, elementPath, result);
            final Vocabulary vocab = getVocabulary(vn);
            result.computeIfAbsent(xpath, x -> new ContextImpl(x, conceptPath, vocab));
        }

    }

    /**
     * Processes an XML attribute
     */
    protected class AttributeProcessor extends VTDProfileProcessor implements VTDNavProcessor<Map<String, Context>> {

        public AttributeProcessor(VloMappingConfiguration config) {
            super(config);
        }

        @Override
        public void process(VTDNav vn, LinkedList<Token> elementPath, Map<String, Context> result) throws NavException {
            int attributeNameIndex = vn.getAttrVal("name");

            if (attributeNameIndex != -1) {
                final String attributeName = vn.toNormalizedString(attributeNameIndex);
                final String xpath = createXpath(elementPath, attributeName);
                // TODO: fix failure at this point because of concept path with null elements!!!!
                final List<String> conceptPath = ImmutableList.copyOf(getConceptPath(vn, elementPath, result));
                final Vocabulary vocab = getVocabulary(vn);
                result.computeIfAbsent(xpath, x -> new ContextImpl(x, conceptPath, vocab));
            }
        }

    }

    /**
     * "this is where the magic happens". Finds paths in the xsd to all concepts
     * (isocat data catagories).
     *
     * @param profileId Id of CMDI profile the component registry
     * @return Map XPath -> Context (includes xpath, concept path and
     * vocabulary)
     * @throws NavException
     */
    @Override
    public Map<String, Context> createProfileContextMap(String profileId) throws NavException {
        return xsdWalker.walkProfile(profileId);
    }

}
