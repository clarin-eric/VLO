package eu.clarin.cmdi.vlo.normalization.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.vlo.normalization.service.NormalizationVocabulary;
import eu.clarin.cmdi.vlo.normalization.service.VocabularyEntry;

/**
 * @author dostojic
 *
 */


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
public class VariantsMap{
	
	@XmlAttribute
	private String field;
	
	@XmlElement(name = "mapping")
	private List<Mapping> mappings;

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
     * returns inverted map: variant-normalizedVal
     *
     */
	
	public Map<String, String> getInvertedMap(){
		Map<String, String> invMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		for(Mapping m: mappings){
			if(m.getVariants() != null)
				for(Variant variant: m.getVariants())
					if(!variant.isRegExp())
						invMap.put(variant.getValue(), m.getValue());
		}
		
		return invMap;
	}
	
	
	public NormalizationVocabulary getMap(){
		List<VocabularyEntry> listOfEntries = new ArrayList<VocabularyEntry>();
		boolean containsRegEx = false;
		
		for(Mapping m: mappings)
			if(m.getVariants() != null)
				for(Variant v: m.getVariants()){					
					listOfEntries.add(new VocabularyEntry(v.getValue().trim(), m.getValue().trim(), v.isRegExp(), v.getCrossMappings()));
					if(v.isRegExp())
						containsRegEx = true;
				}

		return new NormalizationVocabulary(listOfEntries.toArray(new VocabularyEntry[0]), containsRegEx); 
		
	}
	
}
