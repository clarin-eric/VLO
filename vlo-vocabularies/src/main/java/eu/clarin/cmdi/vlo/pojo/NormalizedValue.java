package eu.clarin.cmdi.vlo.pojo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * @author dostojic
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "normalizedValue")
public class NormalizedValue {
	
	@XmlAttribute
	private String value;
	
	public String getValue(){
		return value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
