package eu.clarin.cmdi.vlo.normalization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.clarin.cmdi.vlo.pojo.CrossMapping;

public class VocabularyEntry {
	
	private String originalVal;
	private String normalizedValue;
	private boolean isRegEx;
	private Map<String, String> crossMap;	
	
	public VocabularyEntry(String originalVal, String normalizedValues, boolean isRegEx, List<CrossMapping> crossMap) {
		this.originalVal = originalVal;
		this.normalizedValue = normalizedValues;
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
	
	public String getNormalizedValue() {
		return normalizedValue;
	}
	
	public void setNormalizedValue(String normalizedValue) {
		this.normalizedValue = normalizedValue;
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
		return originalVal + " -> " + normalizedValue + ", isRegEx=" + isRegEx + ", " + crossMap.toString(); 
	}

}
