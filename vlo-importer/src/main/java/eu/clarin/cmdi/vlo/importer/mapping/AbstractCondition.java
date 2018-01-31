package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.vlo.importer.FacetConfiguration;

/**
 * @author @author Wolfgang Walter SAUER (wowasa)
 *         &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public abstract class AbstractCondition {
    private final List<FacetConfiguration> targetFacets;

    public AbstractCondition() {
        this.targetFacets = new ArrayList<FacetConfiguration>();
    }

    public abstract void setPattern(String pattern);

    public abstract boolean matches(String expression);

    public void addTargetFacet(FacetConfiguration facetConfiguration) {
        this.targetFacets.add(facetConfiguration);
    }

    public List<FacetConfiguration> getTargetFacets() {
        return this.targetFacets;
    }
}
