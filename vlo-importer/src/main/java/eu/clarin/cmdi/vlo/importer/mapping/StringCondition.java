package eu.clarin.cmdi.vlo.importer.mapping;

public class StringCondition extends AbstractCondition {
	private String expression;
	private final boolean isCaseSensitive;
	
	public StringCondition(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
	}
	
	public StringCondition(String expression, boolean isCaseSensitive) {
		this.expression = expression;
		this.isCaseSensitive = isCaseSensitive;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public boolean matches(String expression) {
		return this.isCaseSensitive? this.expression.equals(expression):this.expression.equalsIgnoreCase(expression);
	}

}
