package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
public class VariantsMap {

	private final static Logger LOG = LoggerFactory.getLogger(VariantsMap.class);
	
	@XmlAttribute
	private String field;
	
	@XmlElement(name = "mapping")
	public List<Mapping> mappings;

	public List<Mapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}
		
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
	
	
	 /**
     * returns inverted map variant-normalizedVal
     *
     */
	
	public Map<String, String> getInvertedMap(){
		Map<String, String> invMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		for(Mapping m: mappings)
			for(Variant variant: m.getVariants())
				if(!variant.isRegExp())
					invMap.put(variant.getValue(), m.getValue());
		
		return invMap;
	}
	
	
	
	public Map<String, List<Variant>> getMap(){
		Map<String, List<Variant>> map = new HashMap<String, List<Variant>>();
		for(Mapping m: mappings)
			map.put(m.value, m.variants);
		
		return map;
	}


	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "mapping")
	public static class Mapping{
				
		private String value;
		
		@XmlElement(name = "variant")
		List<Variant> variants = new ArrayList<Variant>();
		
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public List<Variant> getVariants() {
			return variants;
		}

		public void setVariants(List<Variant> variants) {
			this.variants = variants;
		}
		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "variant")
	public static class Variant{
		
		@XmlValue
		private String value;
		
		@XmlAttribute
		private boolean isRegExp = false;
		
		public boolean isRegExp() {
			return isRegExp;
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
		
	}
	
}
