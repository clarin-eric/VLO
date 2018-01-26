package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.vlo.importer.FacetConceptMapping;
import eu.clarin.cmdi.vlo.importer.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.FacetConceptMapping.FacetConcept;

public class ValueMappingsHandler extends DefaultHandler {
	
	
	private final List<FacetConcept> facetConcepts;
	
	private final Map<String, List<ConditionTargetSet>> conditionTargetSetPerFacet;
	
	List<Target> targetFacets;
	
	AbstractCondition condition;
	Target target;
	
	List<ConditionTargetSet> conditionTargetSets;
	
	ConditionTargetSet conditionTargetSet;
	
	String value;
	
	public ValueMappingsHandler(FacetConceptMapping facetConceptMapping, Map<String, List<ConditionTargetSet>> conditionTargetSetPerFacet) {
		this.conditionTargetSetPerFacet = conditionTargetSetPerFacet;
		
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

			
			
			break;
		case "value-map":

			break;
		case "target-facet":
			
			break;
		case "target-value-set":
			this.conditionTargetSets.add(new ConditionTargetSet());
			break;
		case "target-value":
			this.target.setValue(this.value);
			this.conditionTargetSet.addTarget(this.target);
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
		switch(qName) {
		case "origin-facet":
			
			
			
			break;
		case "value-map":
			this.targetFacets = new ArrayList<Target>();
			break;
		case "target-facet":
			FacetConcept facetConcept = this.facetConcepts.stream().filter(fc -> fc.getName().equals(attributes.getValue("name"))).findFirst().orElse(null);
			
			if(facetConcept == null)
				return; //warning for reference to a facet which hasn't been defined
			
			FacetConfiguration facetConfiguration = new FacetConfiguration(null);
			facetConfiguration.setAllowMultipleValues(facetConcept.isAllowMultipleValues());
			facetConfiguration.setCaseInsensitive(facetConcept.isCaseInsensitive());
			
			this.targetFacets.add(
					new Target(
							facetConfiguration, 
							attributes.getValue("overrideExistingValues"), 
							attributes.getValue("removeSourceValue")
						)
					);
			break;
		case "target-value-set":
			break;
		case "target-value":
			if(attributes.getValue("facet") == null) { // target-facets setting will be taken
				return;
				
			}
			this.target = this.targetFacets.stream().filter(t -> t.getFacetConfiguration().getName().equals(attributes.getValue("facet"))).findFirst().orElse(null);
			
			if(this.target != null) {
				
			}
			break;
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
