package eu.clarin.cmdi.vlo.pages;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.config.WebAppConfig;

/**
 * Little helper class that creates HTML forms as String
 * 
 * @author Thomas Eckart
 *
 */
public class HtmlFormCreator {
	/**
	 * Creates an HTML form for the content search (FCS).
	 * @param aggregationContextMap Mapping CQL endpoint -> List of resource IDs
	 * @return HTML content search form as String
	 * @throws UnsupportedEncodingException
	 */
	public static String getContentSearchForm(Map<String, List<String>> aggregationContextMap) throws UnsupportedEncodingException {
		JSONObject aggregationJson = new JSONObject();
		Iterator<String> aggregationContextIter = aggregationContextMap.keySet().iterator();
		while(aggregationContextIter.hasNext()) {
			String endpoint = aggregationContextIter.next();
			JSONArray idArray = new JSONArray();
			for(String id : aggregationContextMap.get(endpoint)) {
				idArray.add(id);
			}
			aggregationJson.put(endpoint, idArray);
		}		
		
		String form = "<form method=\"post\" action=\""+WebAppConfig.get().getFederatedContentSearchUrl()+"\"> \n"
				+ "<fieldset style=\"border:0px;\"> \n"
				+ "\t  <label for=\"query\">CQL query:</label> \n"
				+ "\t <input id=\"query\" type=\"text\" name=\"query\" size=\"30\" /> \n"
				+ "\t <input type=\"hidden\" name=\"x-aggregation-context\" value=\'"+aggregationJson.toString(2)+"\' /> \n"
				+ "\t <input type=\"hidden\" name=\"operation\" value=\"searchRetrieve\" /> \n"
				+ "\t <input type=\"hidden\" name=\"version\" value=\"1.2\" /> \n"
				+ "\t <input type=\"submit\" value=\"Send query\" /> \n"
				+ "</fieldset> \n"
				+ "</form> \n";
		return form;	
	}
}
