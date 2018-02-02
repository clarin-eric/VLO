package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.FacetConcept;

/**
 * @author @author Wolfgang Walter SAUER (wowasa)
 *         &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class ValueMappingsHandler extends DefaultHandler {
    private final static Logger LOG = LoggerFactory.getLogger(ValueMappingsHandler.class);

    // initialized via constructor
    private final List<FacetConcept> facetConcepts;
    private final Map<String, List<ConditionTargetSet>> conditionTargetSetsPerFacet;

    // shortcuts to prevent many lookups of last Element in a List
    private List<ConditionTargetSet> conditionTargetSets;
    private ConditionTargetSet conditionTargetSet;

    private Condition condition;
    private TargetFacet targetFacet;

    private List<TargetFacet> targetFacets;
    private String value;

    public ValueMappingsHandler(FacetConceptMapping facetConceptMapping,
            Map<String, List<ConditionTargetSet>> conditionTargetSetsPerFacet) {
        this.conditionTargetSetsPerFacet = conditionTargetSetsPerFacet;

        this.facetConcepts = facetConceptMapping.getFacetConcepts();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.value = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
        case "target-value":
            this.conditionTargetSet.getTargets().stream().forEach(facet -> {
                if (facet.getValue() == null)
                    facet.setValue(this.value);
            });
            break;
        case "source-value":
            this.condition.setExpression(this.value);
            this.conditionTargetSet.addCondition(this.condition);
            break;
        default:

        }

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        FacetConcept facetConcept;
        FacetConfiguration facetConfiguration;

        switch (qName) {
        case "origin-facet":
            this.conditionTargetSets = new ArrayList<ConditionTargetSet>();
            this.conditionTargetSetsPerFacet.put(attributes.getValue("name"), conditionTargetSets);
            break;
        case "value-map":
            this.targetFacets = new ArrayList<TargetFacet>();
            break;
        case "target-facet":
            facetConcept = this.facetConcepts.stream().filter(fc -> fc.getName().equals(attributes.getValue("name")))
                    .findFirst().orElse(null);

            if (facetConcept == null) {
                LOG.warn("no facet concept for target-facet " + attributes.getValue("name"));
                return; // warning for reference to a facet which hasn't been defined
            }

            facetConfiguration = new FacetConfiguration(null, attributes.getValue("name"));
            facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
            facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());

            this.targetFacets.add(new TargetFacet(facetConfiguration, attributes.getValue("overrideExistingValues"),
                    attributes.getValue("removeSourceValue")));
            break;
        case "target-value-set":
            this.conditionTargetSet = new ConditionTargetSet();
            this.conditionTargetSets.add(this.conditionTargetSet);
            break;
        case "target-value":
            if (attributes.getValue("facet") == null) { // clone targetfacets

                this.targetFacets.forEach(facet -> {

                    this.targetFacet = facet.clone();
                    this.conditionTargetSet.addTarget(this.targetFacet);

                    if (attributes.getValue("overrideExistingValues") != null) // only override if attribute is set
                        this.targetFacet.setOverrideExistingValues(attributes.getValue("overrideExistingValues"));
                    if (attributes.getValue("removeSourceValue") != null) // only override if attribute is set
                        this.targetFacet.setRemoveSourceValue(attributes.getValue("removeSourceValue"));// should always be true if not set explicitly

                });
                break;
            }
            // look up a general setting if a facet is set
            this.targetFacet = this.targetFacets.stream()
                    .filter(facet -> facet.getFacetConfiguration().getName().equals(attributes.getValue("facet")))
                    .findFirst().orElse(null);

            if (this.targetFacet != null) { // there is a general setting
                this.targetFacet = this.targetFacet.clone(); // going on with the clone
                if (attributes.getValue("overrideExistingValues") != null) // only override if attribute is set
                    this.targetFacet.setOverrideExistingValues(attributes.getValue("overrideExistingValues"));
                if (attributes.getValue("removeSourceValue") != null) // only override if attribute is set
                    this.targetFacet.setRemoveSourceValue(attributes.getValue("removeSourceValue"));

                this.conditionTargetSet.addTarget(this.targetFacet);
                break;
            }

            facetConcept = this.facetConcepts.stream().filter(fc -> fc.getName().equals(attributes.getValue("facet")))
                    .findFirst().orElse(null);

            if (facetConcept == null) {
                LOG.warn("no facet concept for target-facet " + attributes.getValue("facet"));
                break; // warning for reference to a facet which hasn't been defined
            }

            facetConfiguration = new FacetConfiguration(null, attributes.getValue("facet"));
            facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
            facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());

            this.conditionTargetSet.addTarget(new TargetFacet(facetConfiguration,
                    attributes.getValue("overrideExistingValues"), attributes.getValue("removeSourceValue")));
            break;
        case "source-value":
            this.condition = new Condition(attributes.getValue("isRegex"), attributes.getValue("caseSensitive"));

            break;
        default:

        }
    }

}
