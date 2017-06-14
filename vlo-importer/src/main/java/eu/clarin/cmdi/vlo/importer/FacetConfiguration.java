package eu.clarin.cmdi.vlo.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Once created contains the information about the facets and such. Just a
 * container for some information, doesn't do processing.
 */
public class FacetConfiguration {

    private String name;
    private boolean caseInsensitive = false;
    private List<String> patterns = new ArrayList<>();
    private List<String> fallbackPatterns = new ArrayList<>();
    private List<String> derivedFacets = new ArrayList<>();
    private boolean allowMultipleValues = true;
    // allow multiple values for the same XPath, even if allowMultipleValues == false
    // (for example for CMD elements with multilingual == yes)
    private boolean multilingual = false;

    public void setCaseInsensitive(boolean caseValue) {
        this.caseInsensitive = caseValue;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    public void setFallbackPatterns(List<String> fallbackPatterns) {
        this.fallbackPatterns = fallbackPatterns;
    }

    public void setPattern(String pattern) {
        this.patterns = Collections.singletonList(pattern);
    }

    public void setFallbackPattern(String fallbackPattern) {
        this.fallbackPatterns = Collections.singletonList(fallbackPattern);
    }

    /**
     * @return List of Strings which are xpaths expressions.
     */
    public List<String> getPatterns() {
        return patterns;
    }

    public List<String> getFallbackPatterns() {
        return fallbackPatterns;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "name=" + name + ", pattern=" + patterns;
    }

    public boolean getAllowMultipleValues() {
        return allowMultipleValues;
    }

    public void setAllowMultipleValues(boolean allowMultipleValues) {
        this.allowMultipleValues = allowMultipleValues;
    }

    public boolean getMultilingual() {
        return multilingual;
    }

    public void setMultilingual(boolean multilingual) {
        this.multilingual = multilingual;
    }

    public List<String> getDerivedFacets() {
        return derivedFacets;
    }

    public void setDerivedFacets(List<String> derivedFacets) {
        this.derivedFacets = derivedFacets;
    }

}
