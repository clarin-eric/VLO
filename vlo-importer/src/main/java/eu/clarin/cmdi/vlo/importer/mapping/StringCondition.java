package eu.clarin.cmdi.vlo.importer.mapping;

public class StringCondition extends AbstractCondition {
    private String pattern;
    private final boolean isCaseSensitive;

    public StringCondition(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    public StringCondition(String pattern, boolean isCaseSensitive) {
        this.pattern = pattern;
        this.isCaseSensitive = isCaseSensitive;
    }

    public void setPattern(String expression) {
        this.pattern = expression;
    }

    @Override
    public boolean matches(String expression) {
        return this.isCaseSensitive ? this.pattern.equals(expression) : this.pattern.equalsIgnoreCase(expression);
    }

}
