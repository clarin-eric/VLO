/**
 * 
 */
package eu.clarin.cmdi.vlo.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author WolfgangWalter SAUER (wowasa) <wolfgang.sauer@oeaw.ac.at>
 *
 */
public class CFMHandler extends DefaultHandler {
	private final static Logger LOG = LoggerFactory.getLogger(CFMHandler.class);
	private FacetMapping mapping;
	private FacetConfiguration orginFacet;

	public CFMHandler(FacetMapping mapping) {
		super();
		this.mapping = mapping;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if("origin-facet".equals(qName))
			this.orginFacet = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if("origin-facet".equals(qName)){
			this.orginFacet = this.mapping.getFacetConfiguration(attributes.getValue("name"));
			if(this.orginFacet == null)
				LOG.error("no facet definition found for orgin-facet " + attributes.getValue("name"));
			return;
		}	
		else if(this.orginFacet != null && "condition".equals(qName)){
			this.orginFacet.addCondition(new CFMCondition(attributes.getValue("ifvalue")));
			return;
		}
		else if(this.orginFacet != null && "cross-facet".equals(qName)){

			FacetConfiguration crossFacet = this.mapping.getFacetConfiguration(attributes.getValue("name"));
			this.orginFacet.getConditions().get(this.orginFacet.getConditions().size() -1).addFacetValuePair(crossFacet, attributes.getValue("setvalue"));;


		}
		
	}
}
