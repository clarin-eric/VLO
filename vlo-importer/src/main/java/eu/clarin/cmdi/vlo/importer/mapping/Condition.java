package eu.clarin.cmdi.vlo.importer.mapping;

public class Condition {
    private boolean isRegEx;
    private boolean isCaseSensitive;
    private String expression;
    
    public Condition(String isRegEx, String isCaseSensitive) {
        this.isRegEx = "true".equalsIgnoreCase(isRegEx);
        this.isCaseSensitive = "true".equalsIgnoreCase(isCaseSensitive);
    }
    public boolean isRegEx() {
        return isRegEx;
    }
    public void setRegEx(boolean isRegEx) {
        this.isRegEx = isRegEx;
    }
    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }
    public void setCaseSensitive(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }
    public String getExpression() {
        return expression;
    }
    public void setExpression(String expression) {
        this.expression = expression;
    }
}
