package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.vlo.importer.FacetConceptMapping;
import eu.clarin.cmdi.vlo.importer.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.FacetConceptMapping.FacetConcept;

public class ValueMappingsHandler extends DefaultHandler {
	private final static Logger LOG = LoggerFactory.getLogger(ValueMappingsHandler.class);
	
	//initialized via constructor
	private final List<FacetConcept> facetConcepts;
	private final Map<String, List<ConditionTargetSet>> conditionTargetSetsPerFacet;
	
	// shortcuts to prevent many lookups of last Element in a List
	private List <ConditionTargetSet> conditionTargetSets;
	private ConditionTargetSet conditionTargetSet;
	
	private AbstractCondition condition;
	private TargetFacet targetFacet;
	
	private List<TargetFacet> targetFacets;
	private String value;
	
	

	
	
	public ValueMappingsHandler(FacetConceptMapping facetConceptMapping, Map<String, List<ConditionTargetSet>> conditionTargetSetsPerFacet) {
		this.conditionTargetSetsPerFacet = conditionTargetSetsPerFacet;
		
		this.facetConcepts = facetConceptMapping.getFacetConcepts();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.value = new String(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch(qName) {
		case "target-value":
			this.conditionTargetSet.getTargets().stream().forEach(facet ->{
				if(facet.getValue() == null)
					facet.setValue(this.value);
			});
			break;
		case "source-value":
			this.condition.setExpression(this.value);
			this.conditionTargetSet.addCondtion(this.condition);
			break;
		default:
				
		}		

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		FacetConcept facetConcept;
		FacetConfiguration facetConfiguration;
		
		
		switch(qName) {
		case "origin-facet":
			this.conditionTargetSets = new ArrayList<ConditionTargetSet>();			
			this.conditionTargetSetsPerFacet.put(attributes.getValue("name"), conditionTargetSets);				
			break;
		case "value-map":
			this.targetFacets = new ArrayList<TargetFacet>();					
			break;
		case "target-facet":
			facetConcept = this.facetConcepts.stream().filter(fc -> fc.getName().equals(attributes.getValue("name"))).findFirst().orElse(null);
			
			if(facetConcept == null) {
				LOG.warn("no facet concept for target-facet " + attributes.getValue("name"));
				return; //warning for reference to a facet which hasn't been defined
			}
			
			facetConfiguration = new FacetConfiguration(null, attributes.getValue("name"));
			facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
			facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());
			
			this.targetFacets.add(new TargetFacet(
					facetConfiguration, 
					attributes.getValue("overrideExistingValues"), 
					attributes.getValue("removeSourceValue")
					)
				);
			break;
		case "target-value-set":
			this.conditionTargetSet = new ConditionTargetSet();	
			this.conditionTargetSets.add(this.conditionTargetSet);
			break;
		case "target-value":
			if(attributes.getValue("facet") == null){ //clone targetfacets
				
				
				this.targetFacets.forEach(facet -> {
					
					this.targetFacet = facet.clone();
					this.conditionTargetSet.addTarget(this.targetFacet);
					
					if(attributes.getValue("overrideExistingValues") != null) //only override if attribute is set
						this.targetFacet.setOverrideExistingValues("true".equals(attributes.getValue("overrideExistingValues")));
					if(attributes.getValue("removeSourceValue") != null) //only override if attribute is set
						this.targetFacet.setRemoveSourceValue("true".equals(attributes.getValue("removeSourceValue")));
					
					
				});
				break;
			}
			// look up a general setting if a facet is set
			this.targetFacet = this.targetFacets.stream().filter(facet -> facet.getFacetConfiguration().getName().equals(attributes.getValue("facet"))).findFirst().orElse(null);
			
			if(this.targetFacet != null) { //there is a general setting
				this.targetFacet = this.targetFacet.clone(); //going on with the clone
				if(attributes.getValue("overrideExistingValues") != null) //only override if attribute is set
					this.targetFacet.setOverrideExistingValues("true".equals(attributes.getValue("overrideExistingValues")));
				if(attributes.getValue("removeSourceValue") != null) //only override if attribute is set
					this.targetFacet.setRemoveSourceValue("true".equals(attributes.getValue("removeSourceValue")));
				
				this.conditionTargetSet.addTarget(this.targetFacet);
				break;
			}
			
			
			facetConcept = this.facetConcepts.stream().filter(fc -> fc.getName().equals(attributes.getValue("facet"))).findFirst().orElse(null);
			
			if(facetConcept == null) {
				LOG.warn("no facet conecpt for target-facet " + attributes.getValue("facet"));
				return; //warning for reference to a facet which hasn't been defined
			}
			
			facetConfiguration = new FacetConfiguration(null, attributes.getValue("facet"));
			facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
			facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());
			
			this.conditionTargetSet.addTarget(
					new TargetFacet(
							facetConfiguration, 
							attributes.getValue("overrideExistingValues"), 
							attributes.getValue("removeSourceValue")
							)
					);			
			return;
		case "source-value":
			if("true".equalsIgnoreCase(attributes.getValue("isRegex")))
				this.condition = new RegExCondition();
				
			else
				this.condition = new StringCondition("true".equalsIgnoreCase(attributes.getValue("caseSensitive")));

			this.conditionTargetSet.addCondtion(this.condition);
			break;
		default:
				
		}
	}

}
