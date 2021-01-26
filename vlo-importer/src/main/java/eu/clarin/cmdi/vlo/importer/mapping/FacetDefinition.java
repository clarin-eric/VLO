package eu.clarin.cmdi.vlo.importer.mapping;

import eu.clarin.cmdi.vlo.facets.configuration.Facet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.clarin.cmdi.vlo.importer.Pattern;

/**
 * Once created contains the information about the facets and such. Just a
 * container for some information, doesn't do processing.
 */
public class FacetDefinition implements Serializable {

    private FacetsMapping mapping;

    private String name;
    private boolean caseInsensitive = false;
    private List<Pattern> patterns = new ArrayList<>();
    private List<Pattern> fallbackPatterns = new ArrayList<>();
    private List<FacetDefinition> derivedFacets = new ArrayList<>();
    private Facet facetConfiguration;

    private ConditionTargetSet conditionTargetSet;

    private boolean allowMultipleValues = true;
    // allow multiple values for the same XPath, even if allowMultipleValues == false
    // (for example for CMD elements with multilingual == yes)
    private boolean multilingual = false;

    public FacetDefinition(FacetsMapping mapping, String name) {
        this.mapping = mapping;
        this.name = name;
    }

    public void setCaseInsensitive(boolean caseValue) {
        this.caseInsensitive = caseValue;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public void setFallbackPatterns(List<Pattern> fallbackPatterns) {
        this.fallbackPatterns = fallbackPatterns;
    }

    public void setPattern(Pattern pattern) {
        this.patterns = Collections.singletonList(pattern);
    }

    public void setFallbackPattern(Pattern fallbackPattern) {
        this.fallbackPatterns = Collections.singletonList(fallbackPattern);
    }

    /**
     * @return List of Strings which are xpaths expressions.
     */
    public List<Pattern> getPatterns() {
        return patterns;
    }

    public List<Pattern> getFallbackPatterns() {
        return fallbackPatterns;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ConditionTargetSet getConditionTargetSet() {
        return this.conditionTargetSet;
    }

    public void setConditionTargetSet(ConditionTargetSet conditionTargetSet) {
        this.conditionTargetSet = conditionTargetSet;
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

    public List<FacetDefinition> getDerivedFacets() {
        return derivedFacets;
    }

    public void addDerivedFacet(FacetDefinition derivedFacet) {
        this.derivedFacets.add(derivedFacet);
    }

    public FacetsMapping getFacetMapping() {
        return this.mapping;
    }

    public void setFacetMapping(FacetsMapping mapping) {
        this.mapping = mapping;
    }

    public void setPropertiesFromConfig(Facet facet) {
        if (facet != null) {
            if (facet.isCaseInsensitive() != null) {
                setCaseInsensitive(facet.isCaseInsensitive());
            }

            if (facet.isAllowMultipleValues() != null) {
                setAllowMultipleValues(facet.isAllowMultipleValues());
            }

            if (facet.isMultilingual() != null) {
                setMultilingual(facet.isMultilingual());
            }
        }
    }
}
