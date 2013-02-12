package eu.clarin.cmdi.vlo.importer;

import java.util.HashMap;
import java.util.Map;

import eu.clarin.cmdi.vlo.FacetConstants;

public class ResourceTypePostProcessor implements PostProcessor {

	private Map<String, String> resourceTypeMap = null;

	@Override
	public String process(String value) {
		String result = value;
		if (value != null) {
			String newVal = getResourceTypeMap().get(value.trim().toLowerCase());
			if (newVal != null) {
				result = newVal;
			}
		}
		return result;
	}

	private Map<String, String> getResourceTypeMap() {
		if (resourceTypeMap == null) {
			resourceTypeMap = new HashMap<String, String>();
			// OLAC DCMIType values.
			resourceTypeMap.put("still image", FacetConstants.RESOURCE_TYPE_IMAGE);
			resourceTypeMap.put("sound", FacetConstants.RESOURCE_TYPE_AUDIO);
			resourceTypeMap.put("moving image", FacetConstants.RESOURCE_TYPE_VIDEO);
			resourceTypeMap.put("text", FacetConstants.RESOURCE_TYPE_TEXT); // Transformes uppercase Text -> text
			resourceTypeMap.put("audio/mp3", FacetConstants.RESOURCE_TYPE_AUDIO);
			resourceTypeMap.put("audio/mpeg", FacetConstants.RESOURCE_TYPE_AUDIO);
			resourceTypeMap.put("audio/wav", FacetConstants.RESOURCE_TYPE_AUDIO);
			resourceTypeMap.put("audio/x-wav", FacetConstants.RESOURCE_TYPE_AUDIO);
			
			resourceTypeMap.put("text/xml", FacetConstants.RESOURCE_TYPE_TEXT);
			resourceTypeMap.put("text/html", FacetConstants.RESOURCE_TYPE_TEXT);
		}
		return resourceTypeMap;
	}

}
