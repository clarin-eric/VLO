package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConceptMapping.FacetConcept;

public class ValueMappingFactorySAXImpl implements ValueMappingFactory {
    private final static Logger LOG = LoggerFactory.getLogger(ValueMappingFactorySAXImpl.class);
    
    private class ValueMappingsHandler extends DefaultHandler {

        // initialized via constructor
        private final List<FacetConcept> facetConcepts;
        private final FacetMapping facetMapping;


        private ConditionTargetSet conditionTargetSet;

        private Condition condition;
        private TargetFacet targetFacet;

        private List<Condition> conditions;
        private List<TargetFacet> defaultFacets, targetFacets;
        private String value;

        public ValueMappingsHandler(FacetConceptMapping facetConceptMapping,
                FacetMapping facetMapping) {
            this.facetMapping = facetMapping;

            this.facetConcepts = facetConceptMapping.getFacetConcepts();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            this.value = new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
            case "target-value-set":
                this.conditions.forEach(condition -> {
                        this.conditionTargetSet.addConditionTarget(condition.isRegex(), condition.isCaseSensitive(), condition.getExpression(), this.targetFacets);
                    }
                );
                
            case "target-value":
                this.targetFacets.stream().forEach(facet -> {
                    if (facet.getValue() == null)
                        facet.setValue(this.value);
                });
                break;
            case "source-value":
                this.condition.setExpression(this.value);
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
                FacetConfiguration facetConfig = this.facetMapping.getFacetConfiguration(attributes.getValue("name"));
                this.conditionTargetSet = new ConditionTargetSet();
                facetConfig.setConditionTargetSet(conditionTargetSet);
                break;
            case "value-map":
                this.defaultFacets = new ArrayList<TargetFacet>();
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

                this.defaultFacets.add(new TargetFacet(facetConfiguration, attributes.getValue("overrideExistingValues"),
                        attributes.getValue("removeSourceValue")));
                break;
            case "target-value-set":
                this.targetFacets = new ArrayList<TargetFacet>();
                this.conditions = new ArrayList<Condition>();
                break;
            case "target-value":
                if (attributes.getValue("facet") == null) { // clone targetfacets

                    this.defaultFacets.forEach(facet -> {

                        this.targetFacet = facet.clone();
                        this.targetFacets.add(this.targetFacet);

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

                    this.targetFacets.add(this.targetFacet);
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

                this.targetFacets.add(new TargetFacet(facetConfiguration,
                        attributes.getValue("overrideExistingValues"), attributes.getValue("removeSourceValue")));
                break;
            case "source-value":
                this.condition = new Condition(attributes.getValue("isRegex"), attributes.getValue("caseSensitive"));
                this.conditions.add(this.condition);

                break;
            default:

            }
        }
    }
    
    private class Condition{
        private String isRegex, caseSensitive, expression;
        
        public Condition(String isRegex, String caseSensitive) {
            this.isRegex = isRegex;
            this.caseSensitive = caseSensitive;
        }
        
        public void setExpression(String expression) {
            this.expression = expression;
        }
        
        public String isRegex() {
            return this.isRegex;
        }
        
        public String isCaseSensitive() {
            return this.caseSensitive;
        }
        
        public String getExpression() {
            return expression;
        }
    }
    
    public final void createValueMapping(String fileName, FacetConceptMapping conceptMapping, FacetMapping facetMapping){
        
        SAXParserFactory fac = SAXParserFactory.newInstance();
        fac.setXIncludeAware(true);
        fac.setNamespaceAware(true);
        
        

            try {
                fac.newSAXParser().parse(fileName, this.new ValueMappingsHandler(conceptMapping, facetMapping));
            } 
            catch (SAXException | IOException | ParserConfigurationException ex) {
                LOG.error("Value Mappings not initialized!", ex);
            }
    }
}
