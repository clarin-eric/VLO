package eu.clarin.cmdi.vlo.importer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class YearPostProcessor implements PostProcessor {
	private static final Pattern YEAR_PATTERN = Pattern.compile("([0-9]{4})");
	
	@Override
	/**
	 * Tries to identify relevant year substrings in input
	 */
	public String process(final String value) {
		Matcher yearMatcher = YEAR_PATTERN.matcher(value);
		if(yearMatcher.find())
			return  yearMatcher.group(1);

		return value;
	}
}
