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

public class ValueMappingFactoryDOMImpl implements ValueMappingFactory {

    private final static Logger LOG = LoggerFactory.getLogger(ValueMappingFactoryDOMImpl.class);

    public final Map<String, List<ConditionTargetSet>> getValueMappings(String fileName, FacetConceptMapping facetConceptMapping) {
        HashMap<String, List<ConditionTargetSet>> valueMappings = new HashMap<String, List<ConditionTargetSet>>();

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setXIncludeAware(true);
        fac.setNamespaceAware(true);

        try {
            DocumentBuilder builder = fac.newDocumentBuilder();
            Document doc = builder.parse(fileName);

            NodeList originFacets = doc.getElementsByTagName("origin-facet");

            for (int a = 0; a < originFacets.getLength(); a++) {
                Element originFacet = (Element) originFacets.item(a);

                List<ConditionTargetSet> conditionTargetSets = new ArrayList<ConditionTargetSet>();
                valueMappings.put(originFacet.getAttribute("name"), conditionTargetSets);

                NodeList valueMaps = originFacet.getElementsByTagName("value-map");
                processOriginFacet(valueMaps, facetConceptMapping, conditionTargetSets);
            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            LOG.error("Value Mappings not initialized!", ex);
        }

        return valueMappings;
    }

    private void processOriginFacet(NodeList valueMaps, FacetConceptMapping facetConceptMapping, List<ConditionTargetSet> conditionTargetSets) throws DOMException {
        for (int b = 0; b < valueMaps.getLength(); b++) {
            List<TargetFacet> defaultTargets = new ArrayList<TargetFacet>();

            Element valueMap = (Element) valueMaps.item(b);

            NodeList defaultFacets = valueMap.getElementsByTagName("target-facet");

            for (int c = 0; c < defaultFacets.getLength(); c++) {
                Element defaultFacet = (Element) defaultFacets.item(c);
                processTargetFacet(facetConceptMapping, defaultFacet, defaultTargets);
            }

            NodeList targetValueSets = valueMap.getElementsByTagName("target-value-set");

            for (int c = 0; c < targetValueSets.getLength(); c++) {
                ConditionTargetSet conditionTargetSet = new ConditionTargetSet();
                conditionTargetSets.add(conditionTargetSet);

                Element targetValueSet = (Element) targetValueSets.item(c);
                processTargetValueSet(targetValueSet, conditionTargetSet, defaultTargets, facetConceptMapping);
            }
        }
    }

    private void processTargetFacet(FacetConceptMapping facetConceptMapping, Element defaultFacet, List<TargetFacet> defaultTargets) {
        FacetConcept facetConcept = facetConceptMapping.getFacetConcepts()
                .stream()
                .filter(
                        fc -> fc.getName().equals(defaultFacet.getAttribute("name"))
                )
                .findFirst()
                .orElse(null);
        if (facetConcept == null) {
            // warning for reference to a facet which hasn't been defined
            LOG.warn("no facet concept for target-facet " + defaultFacet.getAttribute("name"));
            return;
        }
        FacetConfiguration facetConfiguration = new FacetConfiguration(null, defaultFacet.getAttribute("name"));
        facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
        facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());
        defaultTargets.add(new TargetFacet(facetConfiguration, defaultFacet.getAttribute("overrideExistingValues"), defaultFacet.getAttribute("removeSourceValue")));
    }

    private void processTargetValueSet(Element targetValueSet, ConditionTargetSet conditionTargetSet, List<TargetFacet> defaultTargets, FacetConceptMapping facetConceptMapping) throws DOMException {
        NodeList sourceValues = targetValueSet.getElementsByTagName("source-value");

        for (int d = 0; d < sourceValues.getLength(); d++) {
            Element sourceValue = (Element) sourceValues.item(d);

            conditionTargetSet.addCondition(
                    sourceValue.getAttribute("isRegex"),
                    sourceValue.getAttribute("caseSensitive"),
                    sourceValue.getTextContent()
            );
        }

        NodeList targetValues = targetValueSet.getElementsByTagName("target-value");
        for (int d = 0; d < targetValues.getLength(); d++) {
            Element targetValue = (Element) targetValues.item(d);
            processTargetValue(targetValue, defaultTargets, conditionTargetSet, facetConceptMapping);
        }
    }

    private void processTargetValue(Element targetValue, List<TargetFacet> defaultTargets, ConditionTargetSet conditionTargetSet, FacetConceptMapping facetConceptMapping) throws DOMException {
        if (targetValue.getAttribute("facet").isEmpty()) {
            // clone targetfacets
            processTargetValueNoFacet(defaultTargets, targetValue, conditionTargetSet);
            return;
        }

        // look up a general setting if a facet is set
        TargetFacet targetFacet = defaultTargets.stream()
                .filter(facet -> facet.getFacetConfiguration().getName().equals(targetValue.getAttribute("facet")))
                .findFirst().orElse(null);
        if (targetFacet != null) { 
            // there is a general setting
            processTargetValueForFacet(targetFacet, targetValue, conditionTargetSet);
            return;
        }

        FacetConcept facetConcept = facetConceptMapping.getFacetConcepts()
                .stream()
                .filter(
                        fc -> fc.getName().equals(targetValue.getAttribute("facet"))
                )
                .findFirst()
                .orElse(null);
        if (facetConcept == null) {
            LOG.warn("no facet concept for target-facet " + targetValue.getAttribute("facet"));
            return; // warning for reference to a facet which hasn't been defined
        }
        FacetConfiguration facetConfiguration = new FacetConfiguration(null, targetValue.getAttribute("facet"));
        facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
        facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());
        targetFacet = new TargetFacet(facetConfiguration, targetValue.getAttribute("overrideExistingValues"), targetValue.getAttribute("removeSourceValue"));
        targetFacet.setValue(targetValue.getTextContent());
        conditionTargetSet.addTarget(targetFacet);
    }

    private void processTargetValueNoFacet(List<TargetFacet> defaultTargets, Element targetValue, ConditionTargetSet conditionTargetSet) throws DOMException {
        // clone targetfacets        
        for (TargetFacet defaultTarget : defaultTargets) {
            TargetFacet targetFacet = defaultTarget.clone();

            if (!targetValue.getAttribute("overrideExistingValues").isEmpty()) // only override if attribute is set
            {
                targetFacet.setOverrideExistingValues(targetValue.getAttribute("overrideExistingValues"));
            }
            if (!targetValue.getAttribute("removeSourceValue").isEmpty()) // only override if attribute is set
            {
                targetFacet.setRemoveSourceValue(targetValue.getAttribute("removeSourceValue"));// should always be true if not set explicitly
            }
            targetFacet.setValue(targetValue.getTextContent());
            conditionTargetSet.addTarget(targetFacet);
        }
    }

    private void processTargetValueForFacet(TargetFacet targetFacet, Element targetValue, ConditionTargetSet conditionTargetSet) throws DOMException {
        targetFacet = targetFacet.clone(); // going on with the clone
        if (!targetValue.getAttribute("overrideExistingValues").isEmpty()) // only override if attribute is set
        {
            targetFacet.setOverrideExistingValues(targetValue.getAttribute("overrideExistingValues"));
        }
        if (!targetValue.getAttribute("removeSourceValue").isEmpty()) // only override if attribute is set
        {
            targetFacet.setRemoveSourceValue(targetValue.getAttribute("removeSourceValue"));
        }
        targetFacet.setValue(targetValue.getTextContent());
        conditionTargetSet.addTarget(targetFacet);
    }

}
