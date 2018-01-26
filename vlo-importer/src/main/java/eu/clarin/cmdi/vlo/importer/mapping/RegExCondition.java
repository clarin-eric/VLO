package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.regex.*;

/**
 * @author @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class RegExCondition extends AbstractCondition {
	private Pattern pattern;
	
	public RegExCondition() {
		
	}
	
	public void setExpression(String expression) {
		this.pattern = Pattern.compile(expression);
	}
	
	public RegExCondition(String expression) {
		this.pattern = Pattern.compile(expression);
	}

	@Override
	public boolean matches(String expression) {
		// TODO Auto-generated method stub
		return pattern.matcher(expression).matches();
	}

}
