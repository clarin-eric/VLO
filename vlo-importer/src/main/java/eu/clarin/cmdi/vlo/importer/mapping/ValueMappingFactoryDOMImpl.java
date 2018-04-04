package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.FacetConcept;

/**
 * @author @author Wolfgang Walter SAUER (wowasa)
 * &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class ValueMappingFactoryDOMImpl implements ValueMappingFactory {

    private final static Logger LOG = LoggerFactory.getLogger(ValueMappingFactoryDOMImpl.class);


    /* (non-Javadoc)
     * @see eu.clarin.cmdi.vlo.importer.mapping.ValueMappingFactory#getValueMappings(java.lang.String, eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping)
     */
    public final Map<String, ConditionTargetSet> getValueMappings(String fileName, FacetConceptMapping facetConceptMapping) {
        HashMap<String, ConditionTargetSet> valueMappings = new HashMap<String, ConditionTargetSet>();

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setXIncludeAware(true);
        fac.setNamespaceAware(true);

        try {
            DocumentBuilder builder = fac.newDocumentBuilder();
            Document doc = builder.parse(fileName);

            NodeList originFacets = doc.getElementsByTagName("origin-facet");

            for (int a = 0; a < originFacets.getLength(); a++) {

                Element originFacet = (Element) originFacets.item(a);

                ConditionTargetSet conditionTargetSet = new ConditionTargetSet();
                valueMappings.put(originFacet.getAttribute("name"), conditionTargetSet);

                processOriginFacet(facetConceptMapping, conditionTargetSet, originFacet);
            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            LOG.error("Value Mappings not initialized!", ex);
        }

        return valueMappings;
    }

    /**
     * @param facetConceptMapping
     * @param conditionTargetSet
     * @param originFacetElement
     */
    private void processOriginFacet(FacetConceptMapping facetConceptMapping, ConditionTargetSet conditionTargetSet, Element originFacetElement) {
        NodeList valueMaps = originFacetElement.getElementsByTagName("value-map");

        for (int b = 0; b < valueMaps.getLength(); b++) {

            Element valueMap = (Element) valueMaps.item(b);

            processValueMap(facetConceptMapping, conditionTargetSet, valueMap);

        }
    }

    /**
     * @param facetConceptMapping
     * @param conditionTargetSet
     * @param valueMapElement
     */
    private void processValueMap(FacetConceptMapping facetConceptMapping, ConditionTargetSet conditionTargetSet, Element valueMapElement) {
        List<TargetFacet> defaultTargets = new ArrayList<TargetFacet>();

        NodeList defaultFacets = valueMapElement.getElementsByTagName("target-facet");

        for (int c = 0; c < defaultFacets.getLength(); c++) {
            Element defaultFacet = (Element) defaultFacets.item(c);

            TargetFacet targetFacet = getTargetFacet(facetConceptMapping, defaultFacet.getAttribute("name"), defaultFacet.getAttribute("overrideExistingValues"), defaultFacet.getAttribute("removeSourceValue"), null);

            if (targetFacet != null) {

                defaultTargets.add(targetFacet);
            }
        }

        NodeList targetValueSets = valueMapElement.getElementsByTagName("target-value-set");

        for (int c = 0; c < targetValueSets.getLength(); c++) {

            Element targetValueSet = (Element) targetValueSets.item(c);

            processTargetValueSet(facetConceptMapping, conditionTargetSet, defaultTargets, targetValueSet);

        }
    }

    /**
     * @param facetConceptMapping
     * @param conditionTargetSet
     * @param defaultTargets
     * @param targetValueSetElement
     */
    private void processTargetValueSet(FacetConceptMapping facetConceptMapping, ConditionTargetSet conditionTargetSet, List<TargetFacet> defaultTargets, Element targetValueSetElement) {
        NodeList targetValues = targetValueSetElement.getElementsByTagName("target-value");

        List<TargetFacet> targets = new ArrayList<TargetFacet>();

        for (int d = 0; d < targetValues.getLength(); d++) {
            Element targetValue = (Element) targetValues.item(d);

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

                targets.add(cloneDefaultTarget(targetFacet, targetValue));
                continue;
            }

            targetFacet = getTargetFacet(facetConceptMapping, targetValue.getAttribute("facet"), targetValue.getAttribute("overrideExistingValues"), targetValue.getAttribute("removeSourceValue"), targetValue.getTextContent());

            if (targetFacet != null) {
                targets.add(targetFacet);
            }

        }

        NodeList sourceValues = targetValueSetElement.getElementsByTagName("source-value");

        for (int d = 0; d < sourceValues.getLength(); d++) {
            Element sourceValue = (Element) sourceValues.item(d);

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
        TargetFacet targetFacet = defaultFacet.clone(); // going on with the clone
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
    private TargetFacet getTargetFacet(FacetConceptMapping facetConceptMapping, String facetName, String overrideExistingValues, String removeSourceValue, String value) {
        FacetConcept facetConcept = facetConceptMapping.getFacetConcepts().stream().filter(fc -> fc.getName().equals(facetName))
                .findFirst().orElse(null);

        if (facetConcept == null) {// warning for reference to a facet which hasn't been defined
            LOG.warn("no facet concept for target-facet " + facetName);
            return null;
        }

        FacetConfiguration facetConfiguration = new FacetConfiguration(null, facetName);
        facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
        facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());

        return new TargetFacet(facetConfiguration, overrideExistingValues, removeSourceValue, value);
    }

}
