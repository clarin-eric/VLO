package eu.clarin.cmdi.vlo.pojo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * @author dostojic
 *
 */


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cross-mapping")
public class CrossMapping {
	
	@XmlAttribute
	private String facet;
	
	@XmlAttribute
	private String value;

	public String getFacet() {
		return facet;
	}

	public void setFacet(String facet) {
		this.facet = facet;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return facet + ":" + value;
	}


}
