package eu.clarin.cmdi.vlo.normalization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.clarin.cmdi.vlo.pojo.CrossMapping;

public class VocabularyEntry {
	
	private String originalVal;
	private List<String> normalizedValues;
	private boolean isRegEx;
	private Map<String, String> crossMap;	
	
	public VocabularyEntry(String originalVal, List<String> normalizedValues, boolean isRegEx, List<CrossMapping> crossMap) {
		this.originalVal = originalVal;
		this.normalizedValues = normalizedValues;
		this.isRegEx = isRegEx;
		this.crossMap = new HashMap<String, String>();
		if(crossMap != null)
			for(CrossMapping cm: crossMap){
				this.crossMap.put(cm.getFacet(), cm.getValue());
			}
	}

	public String getOriginalVal() {
		return originalVal;
	}
	
	public void setOriginalVal(String val) {
		this.originalVal = val;
	}
	
	public List<String> getNormalizedValue() {
		return normalizedValues;
	}
	
	public void setNormalizedValue(List<String> normalizedValues) {
		this.normalizedValues = normalizedValues;
	}
	
	public boolean isRegEx() {
		return isRegEx;
	}
	
	public void setRegEx(boolean isRegEx) {
		this.isRegEx = isRegEx;
	}
	
	public Map<String, String> getCrossMap() {
		return crossMap;
	}
	
	public void setCrossMap(Map<String, String> crossMap) {
		this.crossMap = crossMap;
	}
	
	
	@Override
	public String toString() {
		String normalizedVals = "";
		for(String val: normalizedValues)
			normalizedVals += val + ", ";
		return originalVal + " -> " + normalizedValues + ", isRegEx=" + isRegEx + ", " + crossMap.toString(); 
	}

}
