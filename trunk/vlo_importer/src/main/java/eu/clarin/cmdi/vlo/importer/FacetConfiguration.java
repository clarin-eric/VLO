package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Once created contains the information about the facets and such.
 * Just a container for some information, doesn't do processing.
 */

public class FacetConfiguration {

    private String name;
    private boolean caseInsensitive= false;
    private List<String> patterns = new ArrayList<String>();
    private boolean allowMultipleValues = true;

    public void setCaseInsensitive(boolean caseValue) {
        this.caseInsensitive = caseValue;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public void setPattern(String pattern) {
        this.patterns = Collections.singletonList(pattern);
    }

    /**
     * @return List of Strings which are xpaths expressions.
     */
    public List<String> getPatterns() {
        return patterns;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "name="+name+", pattern="+patterns;
    }

    public boolean getAllowMultipleValues() {
        return allowMultipleValues;
    }

    public void setAllowMultipleValues(boolean allowMultipleValues) {
        this.allowMultipleValues = allowMultipleValues;
    }
}
