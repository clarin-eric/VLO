package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.regex.*;

/**
 * @author @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class RegExCondition extends AbstractCondition {
	private final Pattern pattern;
	
	public RegExCondition(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	@Override
	public boolean matches(String expression) {
		// TODO Auto-generated method stub
		return pattern.matcher(expression).matches();
	}

}
