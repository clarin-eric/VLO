package eu.clarin.cmdi.vlo.importer.processor;

import org.apache.commons.lang3.tuple.Pair;

import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.TargetFacet;
import org.apache.commons.lang3.tuple.ImmutablePair;

public class ValueSet {

    private int vtdIndex;
    private FacetConfiguration originFacetConfig;
    private TargetFacet targetFacet;
    private Pair<String, String> valueLanguagePair;
    private boolean isDerived;
    private boolean isResultOfValueMapping;

    public ValueSet(int vtdIndex, FacetConfiguration originFacetConfig, TargetFacet targetFacet, Pair<String, String> valueLanguagePair, boolean isDerived, boolean isResultOfValueMapping) {
        this.vtdIndex = vtdIndex;
        this.originFacetConfig = originFacetConfig;
        this.targetFacet = targetFacet;
        this.valueLanguagePair = valueLanguagePair;
        this.isDerived = isDerived;
        this.isResultOfValueMapping = isResultOfValueMapping;
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

    public String getTargetFacetName() {
        return getTargetFacet().getFacetConfiguration().getName();
    }

    public void setTargetFacet(TargetFacet targetFacet) {
        this.targetFacet = targetFacet;
    }

    public Pair<String, String> getValueLanguagePair() {
        return valueLanguagePair;
    }

    public String getValue() {
        return getValueLanguagePair().getLeft();
    }

    public void setValue(String value) {
        setValueLanguagePair(ImmutablePair.of(value, getLanguage()));
    }

    public String getLanguage() {
        return getValueLanguagePair().getRight();
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

    public boolean isResultOfValueMapping() {
        return isResultOfValueMapping;
    }

    public void setResultOfValueMapping(boolean resultOfValueMapping) {
        this.isResultOfValueMapping = resultOfValueMapping;
    }

    public ValueSet makeCopy() {
        return new ValueSet(vtdIndex, originFacetConfig, targetFacet, Pair.of(valueLanguagePair.getLeft(), valueLanguagePair.getRight()), isDerived, isResultOfValueMapping);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] %s", vtdIndex, getLanguage(), getValue());
    }
    
    

}
