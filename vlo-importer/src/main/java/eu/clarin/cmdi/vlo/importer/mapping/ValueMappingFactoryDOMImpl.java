package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import eu.clarin.cmdi.vlo.MappingDefinitionResolver;
import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.FacetConcept;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizerWithVocabularyMap;
import java.util.Optional;
import org.xml.sax.InputSource;

/**
 * @author @author Wolfgang Walter SAUER (wowasa)
 * &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class ValueMappingFactoryDOMImpl implements ValueMappingFactory {

    private final static Logger LOG = LoggerFactory.getLogger(ValueMappingFactoryDOMImpl.class);
    private final MappingDefinitionResolver mappingDefinitionResolver = new MappingDefinitionResolver(AbstractPostNormalizerWithVocabularyMap.class);


    /* (non-Javadoc)
     * @see eu.clarin.cmdi.vlo.importer.mapping.ValueMappingFactory#getValueMappings(java.lang.String, eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping)
     */
    @Override
    public final void createValueMapping(String fileName, FacetConceptMapping facetConceptMapping, FacetsMapping facetMapping) throws SAXException, IOException, ParserConfigurationException {
        LOG.info("Parsing value mapping in {}", fileName);
        final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setXIncludeAware(true);
        fac.setNamespaceAware(true);
        final InputSource valueMappingDefinitionStream = mappingDefinitionResolver.tryResolveUrlFileOrResourceStream(fileName);
        if (valueMappingDefinitionStream == null) {
            throw new IOException("No input source for " + fileName);
        }
        final DocumentBuilder builder = fac.newDocumentBuilder();
        final Document doc = builder.parse(valueMappingDefinitionStream);

        final NodeList originFacets = doc.getElementsByTagName("origin-facet");

        LOG.info("Found {} origin-facet nodes", originFacets.getLength());
        for (int a = 0; a < originFacets.getLength(); a++) {

            Element originFacet = (Element) originFacets.item(a);

            final FacetDefinition facetDefinition = facetMapping.getFacetDefinition(originFacet.getAttribute("name"));

            final ConditionTargetSet conditionTargetSet = new ConditionTargetSet();
            facetDefinition.setConditionTargetSet(conditionTargetSet);

            processOriginFacet(facetMapping, facetConceptMapping, conditionTargetSet, originFacet);
        }
    }

    /**
     * @param facetConceptMapping
     * @param conditionTargetSet
     * @param originFacetElement
     */
    private void processOriginFacet(FacetsMapping facetMapping, FacetConceptMapping facetConceptMapping, ConditionTargetSet conditionTargetSet, Element originFacetElement) {
        LOG.info("Processing origin-facet node with name='{}'", originFacetElement.getAttribute("name"));
        final NodeList valueMaps = originFacetElement.getElementsByTagName("value-map");

        for (int b = 0; b < valueMaps.getLength(); b++) {

            Element valueMap = (Element) valueMaps.item(b);

            processValueMap(facetMapping, facetConceptMapping, conditionTargetSet, valueMap);

        }
    }

    /**
     * @param facetConceptMapping
     * @param conditionTargetSet
     * @param valueMapElement
     */
    private void processValueMap(FacetsMapping facetMapping, FacetConceptMapping facetConceptMapping, ConditionTargetSet conditionTargetSet, Element valueMapElement) {
        LOG.info("-- Processing value-map node");
        final List<TargetFacet> defaultTargets = new ArrayList<>();

        final NodeList defaultFacets = valueMapElement.getElementsByTagName("target-facet");

        for (int c = 0; c < defaultFacets.getLength(); c++) {
            Element defaultFacet = (Element) defaultFacets.item(c);

            TargetFacet targetFacet = getTargetFacet(facetMapping, facetConceptMapping, defaultFacet.getAttribute("name"), defaultFacet.getAttribute("overrideExistingValues"), defaultFacet.getAttribute("removeSourceValue"), null);

            if (targetFacet != null) {

                defaultTargets.add(targetFacet);
            }
        }

        final NodeList targetValueSets = valueMapElement.getElementsByTagName("target-value-set");
        LOG.info("-- Found {} target-value-set nodes", targetValueSets.getLength());
        for (int c = 0; c < targetValueSets.getLength(); c++) {

            Element targetValueSet = (Element) targetValueSets.item(c);

            processTargetValueSet(facetMapping, facetConceptMapping, conditionTargetSet, defaultTargets, targetValueSet);

        }
    }

    /**
     * @param facetConceptMapping
     * @param conditionTargetSet
     * @param defaultTargets
     * @param targetValueSetElement
     */
    private void processTargetValueSet(FacetsMapping facetMapping, FacetConceptMapping facetConceptMapping, ConditionTargetSet conditionTargetSet, List<TargetFacet> defaultTargets, Element targetValueSetElement) {
        final NodeList targetValues = targetValueSetElement.getElementsByTagName("target-value");
        LOG.debug("-- -- Processing target-value-set with {} target-value node(s)", targetValues.getLength());

        final List<TargetFacet> targets = new ArrayList<>();

        for (int d = 0; d < targetValues.getLength(); d++) {
            Element targetValue = (Element) targetValues.item(d);
            LOG.debug("-- -- Processing target-value node with facet='{}'", targetValue.getAttribute("facet"));

            TargetFacet targetFacet;

            if (targetValue.getAttribute("facet").isEmpty()) { // clone targetfacets

                for (TargetFacet defaultTarget : defaultTargets) {
                    targets.add(cloneDefaultTarget(defaultTarget, targetValue));
                }

                continue;
            }
            // look up a general setting if a facet is set
            targetFacet = defaultTargets.stream()
                    .filter(facet -> facet.getFacetConfiguration().getName().equals(targetValue.getAttribute("facet")))
                    .findFirst().orElse(null);

            if (targetFacet != null) { // there is a general setting
                TargetFacet clonedTargetFacet = cloneDefaultTarget(targetFacet, targetValue);
                targets.add(clonedTargetFacet);
                LOG.debug("-- -- -- Added target value '{}' (cloned target definition: [overrideExistingValues: {}, removeSourceValue: {}])", clonedTargetFacet.getValue(), clonedTargetFacet.getOverrideExistingValues(), clonedTargetFacet.getRemoveSourceValue());
                continue;
            }

            targetFacet = getTargetFacet(facetMapping, facetConceptMapping, targetValue.getAttribute("facet"), targetValue.getAttribute("overrideExistingValues"), targetValue.getAttribute("removeSourceValue"), targetValue.getTextContent());

            if (targetFacet != null) {
                targets.add(targetFacet);
                LOG.debug("-- -- -- Added target value '{}' [overrideExistingValues: {}, removeSourceValue: {}]", targetFacet.getValue(), targetFacet.getOverrideExistingValues(), targetFacet.getRemoveSourceValue());
            }

        }

        final NodeList sourceValues = targetValueSetElement.getElementsByTagName("source-value");
        LOG.debug("-- -- Continued processing of target-value-set with {} source-value node(s)", sourceValues.getLength());

        for (int d = 0; d < sourceValues.getLength(); d++) {
            Element sourceValue = (Element) sourceValues.item(d);
            LOG.debug("-- -- -- Processing source-value node with content '{}'", sourceValue.getTextContent());

            conditionTargetSet.addConditionTarget(
                    sourceValue.getAttribute("isRegex"),
                    sourceValue.getAttribute("caseSensitive"),
                    sourceValue.getTextContent(),
                    targets
            );
        }
    }

    /**
     * @param defaultFacet
     * @param targetValue
     * @return clone off default facet adapted for specific
     * overrideExistingValues/emoveSourceValue if set
     */
    private TargetFacet cloneDefaultTarget(TargetFacet defaultFacet, Element targetValue) {
        final TargetFacet targetFacet = defaultFacet.clone(); // going on with the clone
        if (!targetValue.getAttribute("overrideExistingValues").isEmpty()) // only override if attribute is set
        {
            targetFacet.setOverrideExistingValues(targetValue.getAttribute("overrideExistingValues"));
        }
        if (!targetValue.getAttribute("removeSourceValue").isEmpty()) // only override if attribute is set
        {
            targetFacet.setRemoveSourceValue(targetValue.getAttribute("removeSourceValue"));
        }

        targetFacet.setValue(targetValue.getTextContent());

        return targetFacet;
    }

    /**
     * @param facetConceptMapping
     * @param facetName
     * @param overrideExistingValues
     * @param removeSourceValue
     * @param value
     * @return a new instance of TargetFacet with specific settings for
     * overrideExistingValues, removeSourceValue and a value
     */
    private TargetFacet getTargetFacet(FacetsMapping facetMapping, FacetConceptMapping facetConceptMapping, String facetName, String overrideExistingValues, String removeSourceValue, String value) {
        final FacetConcept facetConcept = facetConceptMapping.getFacetConcepts().stream()
                .filter(fc -> fc.getName().equals(facetName))
                .findFirst()
                .orElse(null);

        if (facetConcept == null) {// warning for reference to a facet which hasn't been defined
            LOG.warn("no facet concept for target-facet " + facetName);
            return null;
        }

        final Optional<Facet> facetConfig = facetMapping.getFacetConfiguration(facetName);

        final FacetDefinition facetDefinition = facetMapping.getFacetDefinition(facetName);
        facetConfig.ifPresent(facetDefinition::setPropertiesFromConfig);

        return new TargetFacet(facetDefinition, overrideExistingValues, removeSourceValue, value);
    }

}
