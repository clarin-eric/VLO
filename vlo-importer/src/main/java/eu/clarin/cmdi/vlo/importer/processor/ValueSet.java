package eu.clarin.cmdi.vlo.importer.processor;

import org.apache.commons.lang3.tuple.Pair;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;

public class ValueSet{
    private int vtdIndex;
    private FacetConfiguration originFacetConfig;
    private TargetFacet targetFacet;
    private Pair<String, String> valueLanguagePair;
    private boolean isDerived;
    
    public ValueSet(int vtdIndex, FacetConfiguration originFacetConfig, TargetFacet targetFacet, Pair<String,String> valueLanguagePair, boolean isDerived) {
        this.vtdIndex = vtdIndex;
        this.originFacetConfig = originFacetConfig;
        this.targetFacet = targetFacet;
        this.valueLanguagePair = valueLanguagePair;
        this.isDerived = isDerived;
    }

    public int getVtdIndex() {
        return vtdIndex;
    }

    public void setVtdIndex(int vtdIndex) {
        this.vtdIndex = vtdIndex;
    }

    public FacetConfiguration getOriginFacetConfig() {
        return originFacetConfig;
    }

    public void setOriginFacetConfig(FacetConfiguration originFacetConfig) {
        this.originFacetConfig = originFacetConfig;
    }

    public TargetFacet getTargetFacet() {
        return targetFacet;
    }

    public void setTargetFacet(TargetFacet targetFacet) {
        this.targetFacet = targetFacet;
    }

    public Pair<String, String> getValueLanguagePair() {
        return valueLanguagePair;
    }

    public void setValueLanguagePair(Pair<String, String> valueLanguagePair) {
        this.valueLanguagePair = valueLanguagePair;
    }

    public boolean isDerived() {
        return isDerived;
    }

    public void setDerived(boolean isDerived) {
        this.isDerived = isDerived;
    }


}
