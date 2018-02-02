package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class ConditionTargetSet {
    private final List<Pattern> patterns;
    private final List<TargetFacet> targets;
    
    private HashSet<String> caseLess;
    private HashSet<String> caseSensitive;

    public ConditionTargetSet() {
        this.patterns = new ArrayList<Pattern>();
        this.caseLess = new HashSet<String>();
        this.caseSensitive = new HashSet<String>();
        this.targets = new ArrayList<TargetFacet>();
    }
    
    public void addCondition(Condition condition) {
        if(condition.isRegEx())
            this.patterns.add(Pattern.compile(condition.getExpression()));
        else if(condition.isCaseSensitive())
            this.caseSensitive.add(condition.getExpression());
        else
            this.caseLess.add(condition.getExpression().toLowerCase());             
    }

    public void addTarget(TargetFacet target) {
        this.targets.add(target);
    }

    public List<TargetFacet> getTargets() {
        return this.targets;
    }

    public boolean matches(String expression) {
        return this.caseLess.contains(expression.trim().toLowerCase()) ||
                this.caseSensitive.contains(expression.trim()) ||
                this.patterns.stream().anyMatch(pattern -> pattern.matcher(expression).matches());
    }
}
