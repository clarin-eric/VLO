package eu.clarin.cmdi.vlo.importer;

public class FacetConfiguration {

    private String name;
    private String pattern;
    private boolean caseInsensitive= false;

    public void setCaseInsensitive(boolean caseValue) {
        this.caseInsensitive = caseValue;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "name="+name+", pattern="+pattern;
    }
}
