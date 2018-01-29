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
	
	private final List<FacetConcept> facetConcepts;
	
	private final Map<String, List<ConditionTargetSet>> conditionTargetSetsPerFacet;
	
	
	private String originFacetName;
	private List <ConditionTargetSet> conditionTargetSets;
	private ConditionTargetSet conditionTargetSet;
	private List<AbstractCondition> conditions;
	private List<TargetFacet> targetFacets;
	private List<TargetFacet> targetValues;
	private AbstractCondition condition;
	private TargetFacet targetFacet;
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
		case "origin-facet":
			this.conditionTargetSetsPerFacet.put(this.originFacetName, this.conditionTargetSets);
			
			
			break;
		case "value-map":

			break;
		case "target-facet":
			
			break;
		case "target-value-set":
			this.conditionTargetSets.add(new ConditionTargetSet());
			break;
		case "target-value":
			this.targetValues.stream().forEach(facet ->{
				if(facet.getValue() == null)
					facet.setValue(this.value);
			});
			this.conditionTargetSet.addTarget(this.targetFacet);
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
		
		switch(qName) {
		case "origin-facet":
			this.originFacetName = attributes.getValue("name");					
			break;
		case "value-map":
			this.targetFacets = new ArrayList<TargetFacet>();
			this.conditionTargetSets = new Vector<ConditionTargetSet>();
			
			break;
		case "target-facet":
			facetConcept = this.facetConcepts.stream().filter(fc -> fc.getName().equals(attributes.getValue("name"))).findFirst().orElse(null);
			
			if(facetConcept == null) {
				LOG.warn("no facet concept for target-facet " + attributes.getValue("name"));
				return; //warning for reference to a facet which hasn't been defined
			}
			
			FacetConfiguration facetConfiguration = new FacetConfiguration(null, attributes.getValue("name"));
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
			this.conditions = new ArrayList<AbstractCondition>();
			this.targetValues = new ArrayList<TargetFacet>();
			break;
		case "target-value":
			if(attributes.getValue("facet") == null){ //clone targetfacets
				ArrayList<TargetFacet> tmpList = new ArrayList<TargetFacet>();
				
				this.targetFacets.forEach(facet -> {
					
					if(attributes.getValue("overrideExistingValues") != null) //only override if attribute is set
						this.targetFacet.setOverrideExistingValues("true".equals(attributes.getValue("overrideExistingValues")));
					if(attributes.getValue("removeSourceValue") != null) //only override if attribute is set
						this.targetFacet.setRemoveSourceValue("true".equals(attributes.getValue("removeSourceValue")));
					
					this.targetValues.add(this.targetFacet);
				});
				return;
			}
			// look up a general setting if a facet is set
			this.targetFacet = this.targetFacets.stream().filter(facet -> facet.getFacetConfiguration().getName().equals(attributes.getValue("facet"))).findFirst().orElse(null);
			
			if(this.targetFacet != null) { //there is a general setting
				this.targetFacet = this.targetFacet.clone();
				if(attributes.getValue("overrideExistingValues") != null) //only override if attribute is set
					this.targetFacet.setOverrideExistingValues("true".equals(attributes.getValue("overrideExistingValues")));
				if(attributes.getValue("removeSourceValue") != null) //only override if attribute is set
					this.targetFacet.setRemoveSourceValue("true".equals(attributes.getValue("removeSourceValue")));
				
				this.targetValues.add(this.targetFacet);
				return;
			}
			
			
			facetConcept = this.facetConcepts.stream().filter(fc -> fc.getName().equals(attributes.getValue("facet"))).findFirst().orElse(null);
			
			if(facetConcept == null) {
				LOG.warn("no facet conecpt for target-facet " + attributes.getValue("facet"));
				return; //warning for reference to a facet which hasn't been defined
			}
			
			facetConfiguration = new FacetConfiguration(null, attributes.getValue("facet"));
			facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
			facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());
			
			this.targetFacet = new TargetFacet(
					facetConfiguration, 
					attributes.getValue("overrideExistingValues"), 
					attributes.getValue("removeSourceValue")
				);			
			return;
		case "source-value":
			if("true".equalsIgnoreCase(attributes.getValue("isRegex"))) {
				this.condition = new RegExCondition();
				return;
				
			}
			this.condition = new StringCondition("true".equalsIgnoreCase(attributes.getValue("caseSensitive")));

			
			break;
		default:
				
		}
	}

}
