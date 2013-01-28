package eu.clarin.cmdi.vlo.pages;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.sf.json.JSONObject;
import eu.clarin.cmdi.vlo.Configuration;

/**
 * Little helper class that creates HTML forms as String
 * 
 * @author Thomas Eckart
 *
 */
public class HtmlFormCreator {
	/**
	 * Creates an HTML form for the content search (FCS).
	 * @param aggregationContextJson information about aggregationContext (CQL endpoint URL + identifier) as JSON
	 * @return HTML content search form as String
	 * @throws UnsupportedEncodingException
	 */
	public static String getContentSearchForm(JSONObject aggregationContextJson) throws UnsupportedEncodingException {
		String json = "<form method=\"post\" action=\""+Configuration.getInstance().getFederatedContentSearchUrl()+"\"> \n"
				+ "<fieldset style=\"border:0px;\"> \n"
				+ "\t  <label for=\"query\">CQL query:</label> \n"
				+ "\t <input id=\"query\" type=\"text\" name=\"query\" size=\"30\" /> \n"
				+ "\t <input type=\"hidden\" name=\"x-aggregation-context\" value=\""+URLEncoder.encode(aggregationContextJson.toString(2), "UTF-8")+"\"> \n"
				+ "\t <input type=\"hidden\" name=\"operation\" value=\"searchRetrieve\"> \n"
				+ "\t <input type=\"hidden\" name=\"version\" value=\"1.2\"> \n"
				+ "\t <input type=\"submit\" value=\"Send query\" /> \n"
				+ "</fieldset> \n"
				+ "</form> \n";
		return json;	
	}
}
