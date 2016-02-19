package eu.clarin.cmdi.vlo.normalization;

import java.util.Map;

public interface NormalizationService {

	
	/*
	 * returns normalized values from vocabulary map or null in case of mismatch
	 * variation can be mapped into mulitiple normalized values, 
	 * 
	 * @param input - value from record
	 * @return normalized values if there is a match otherwise input value
	 * 
	 */
	
	public String normalize(String input);	
	
	
	/*
	 * returns returns cross map for given input value in form of facet/value map or null if cross map doesn't exist for specified value or in case of mismatch 
	 * 
	 * @param input - value from record
	 * @return facet/value Map or null
	 * 
	 */
	
	public Map<String, String> getCrossMappings(String input);
	
}
