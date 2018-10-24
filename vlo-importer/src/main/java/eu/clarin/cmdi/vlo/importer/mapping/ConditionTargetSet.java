package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;


public class ConditionTargetSet implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private HashMap<String, List<TargetFacet>> caseLess;
    private HashMap<String, List<TargetFacet>> caseSensitive;
    
    private final HashMap<Pattern, List<TargetFacet>> patterns;


    public ConditionTargetSet() {
        this.patterns = new HashMap<Pattern, List<TargetFacet>>();
        this.caseLess = new HashMap<String, List<TargetFacet>>();
        this.caseSensitive = new HashMap<String, List<TargetFacet>>();
    }
    
    
    public List<TargetFacet> getTargetsFor(String expression) {
        List<TargetFacet> targets = new ArrayList<TargetFacet>();
        
        if(this.caseLess.containsKey(expression.toLowerCase()))
            targets.addAll(this.caseLess.get(expression.toLowerCase()));
        
        if(this.caseSensitive.containsKey(expression))
            targets.addAll(this.caseSensitive.get(expression));
        
        this.patterns.forEach((k, v) -> {
            if(k.matcher(expression).matches()) targets.addAll(v);
            });
        
        return targets;
    }
    
    public void addConditionTarget(String isRegEx, String isCaseSensitive, String expression, List<TargetFacet> targets) {
        if("true".equalsIgnoreCase(isRegEx))
            this.patterns.put(Pattern.compile(expression), targets);
        else if("true".equalsIgnoreCase(isCaseSensitive))
            this.caseSensitive.put(expression, targets);
        else
            this.caseLess.put(expression.toLowerCase(), targets);           
    }
}
