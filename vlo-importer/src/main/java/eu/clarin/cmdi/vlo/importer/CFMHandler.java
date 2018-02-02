/**
 * 
 */
package eu.clarin.cmdi.vlo.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;

/**
 * @author WolfgangWalter SAUER (wowasa) <wolfgang.sauer@oeaw.ac.at>
 *
 */
public class CFMHandler extends DefaultHandler {
	private final Logger LOG; 
	private FacetMapping mapping;
	private FacetConfiguration orginFacet;

	public CFMHandler(FacetMapping mapping) {
		super();
		this.LOG = LoggerFactory.getLogger(this.getClass());
		this.mapping = mapping;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if("origin-facet".equals(qName))
			this.orginFacet = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
/*		if("origin-facet".equals(qName)){
			this.orginFacet = this.mapping.getFacetConfiguration(attributes.getValue("name"));
			if(this.orginFacet == null)
				LOG.warn("origin facet " + attributes.getValue("name") + " can't be processed since there is NO valid facetConcept defined for this facet");
			return;
		}	
		else if(this.orginFacet != null && "condition".equals(qName)){
			this.orginFacet.addCondition(new CFMCondition(attributes.getValue("ifvalue")));
			return;
		}
		else if(this.orginFacet != null && "cross-facet".equals(qName)){

			FacetConfiguration crossFacet;
			if((crossFacet = this.mapping.getFacetConfiguration(attributes.getValue("name"))) == null)
				LOG.warn("cross facet " + attributes.getValue("name") + " can't be processed since there is NO valid facetConcept defined for this facet");
			else
				this.orginFacet.getConditions().get(this.orginFacet.getConditions().size() -1).addFacetValuePair(crossFacet, attributes.getValue("setvalue"));;
		}*/
	}
}
