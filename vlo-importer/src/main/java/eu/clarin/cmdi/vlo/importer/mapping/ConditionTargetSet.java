package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.ArrayList;
import java.util.List;

public class ConditionTargetSet {
    private final List<AbstractCondition> conditions;
    private final List<TargetFacet> targets;

    public ConditionTargetSet() {
        this.conditions = new ArrayList<AbstractCondition>();
        this.targets = new ArrayList<TargetFacet>();
    }

    public void addCondtion(AbstractCondition condition) {
        this.conditions.add(condition);
    }

    public void addTarget(TargetFacet target) {
        this.targets.add(target);
    }

    public List<TargetFacet> getTargets() {
        return this.targets;
    }

    public boolean matches(String expression) {
        return this.conditions.stream().anyMatch(condition -> condition.matches(expression));
    }

}
