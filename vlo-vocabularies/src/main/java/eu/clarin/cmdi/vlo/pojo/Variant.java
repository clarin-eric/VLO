package eu.clarin.cmdi.vlo.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "variant")
public class Variant{
	
	@XmlAttribute
	private String value;
	
	@XmlAttribute
	private Boolean isRegExp = null;
	
	@XmlElement(name = "cross-mappings")
	private CrossMappings crossMappings;
	
	public Boolean isRegExp() {
		return isRegExp != null ? isRegExp : false;
	}

	public void setRegExp(boolean isRegExp) {
		this.isRegExp = isRegExp;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}	

	public List<CrossMapping> getCrossMappings() {
		return crossMappings != null? crossMappings.getCrossMappings() : null;
	}

	public void setCrossMappings(List<CrossMapping> crossMappings) {
		if(this.crossMappings == null){
			this.crossMappings = new CrossMappings();
		}
		this.crossMappings.setCrossMappings(crossMappings);
	}
	
	@Override
	public String toString() {
		return value + " " + (isRegExp ? "isRegEx = true" : "") + " " + "cross-mappings["+ crossMappings.toString() + "]";
	}

	
}