package eu.clarin.cmdi.vlo.pojo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


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
