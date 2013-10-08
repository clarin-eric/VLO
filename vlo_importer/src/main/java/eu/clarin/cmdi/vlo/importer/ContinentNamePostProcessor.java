package eu.clarin.cmdi.vlo.importer;

import java.util.HashMap;
import java.util.Map;

public class ContinentNamePostProcessor implements PostProcessor {

	private static Map<String, String> continentCodeMap;
	static {
		continentCodeMap = new HashMap<String, String>();
		continentCodeMap.put("AF", "Africa");
		continentCodeMap.put("AS", "Asia");
		continentCodeMap.put("EU", "Europe");
		continentCodeMap.put("NA", "North America");
		continentCodeMap.put("SA", "South America");
		continentCodeMap.put("OC", "Oceania");
		continentCodeMap.put("AN", "Antarctica");
	}

	/**
	 * Replaces two-letter continent codes with continent names
	 */
	@Override
	public String process(final String value) {
		if (value != null && continentCodeMap.keySet().contains(value)) {
			return continentCodeMap.get(value);
		}
		return value;
	}
}
