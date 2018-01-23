package eu.clarin.cmdi.vlo.importer.mapping;

public class StringCondition extends AbstractCondition {
	private final String condition;
	private final boolean isCaseSensitive;
	
	public StringCondition(String condition, boolean isCaseSensitive) {
		this.condition = condition;
		this.isCaseSensitive = isCaseSensitive;
	}

	@Override
	public boolean matches(String expression) {
		return this.isCaseSensitive? this.condition.equals(expression):this.condition.equalsIgnoreCase(expression);
	}

}
