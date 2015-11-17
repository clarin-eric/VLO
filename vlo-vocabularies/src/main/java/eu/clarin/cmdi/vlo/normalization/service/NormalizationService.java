package eu.clarin.cmdi.vlo.normalization.service;

import java.util.Map;

public interface NormalizationService {

	
	/*
	 * returns normalized value from vocabulary map or specified value in case of mismatch
	 * 
	 * @param input - value from record
	 * @param fallbackOutput - specified returned value in case of mismatch 
	 * @return normalized value if there is a match otherwise input value
	 * 
	 */
	
	public String normalize(String input, String fallbackOutput);	
	
	
	/*
	 * returns returns cross map for given input value in form of facet/value map or null if cross map doesn't exist for specified value or in case of mismatch 
	 * 
	 * @param input - value from record
	 * @return facet/value Map or null
	 * 
	 */
	
	public Map<String, String> getCrossMappings(String input);
	
}
