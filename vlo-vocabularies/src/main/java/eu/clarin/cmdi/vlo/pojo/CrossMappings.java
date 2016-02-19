package eu.clarin.cmdi.vlo.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
