package eu.clarin.cmdi.vlo.pojo;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cross-mappings")
public class CrossMappings{

	@XmlElement(name = "cross-mapping")
	private List<CrossMapping> crossMappings;

	public List<CrossMapping> getCrossMappings() {
		return crossMappings;
	}

	public void setCrossMappings(List<CrossMapping> crossMappings) {
		this.crossMappings = crossMappings;
	}

	
	@Override
	public String toString() {
		String s = "";
		for(CrossMapping cm: crossMappings)
			s += " ," + cm.toString();
		return s.replaceFirst(",", "").trim();
	}
	
}
