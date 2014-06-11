package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class YearPostProcessor implements PostProcessor {
	private static final Pattern YEAR_PATTERN = Pattern.compile("([0-9]{4})");
	
	
	/**
	 * Tries to identify relevant year substrings in input
	 */
        @Override
	public List<String> process(final String value) {
		Matcher yearMatcher = YEAR_PATTERN.matcher(value);
                List<String> resultList = new ArrayList<String>();
                
		if(yearMatcher.find())
			resultList.add(yearMatcher.group(1));

		return resultList;
	}
}
