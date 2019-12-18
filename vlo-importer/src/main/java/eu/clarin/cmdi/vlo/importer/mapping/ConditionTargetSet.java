package eu.clarin.cmdi.vlo.importer.mapping;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class ConditionTargetSet implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Map<String, List<TargetFacet>> caseLess = Maps.newHashMap();
    private final Map<String, List<TargetFacet>> caseSensitive = Maps.newHashMap();
    private final Map<Pattern, List<TargetFacet>> patterns = Maps.newHashMap();

    public ConditionTargetSet() {
    }

    public List<TargetFacet> getTargetsFor(String expression) {
        List<TargetFacet> targets = new ArrayList<>();

        final String lowerCaseExpression = Optional.ofNullable(expression)
                .map(String::toLowerCase)
                .orElse(null);

        if (this.caseLess.containsKey(lowerCaseExpression)) {
            targets.addAll(this.caseLess.get(expression.toLowerCase()));
        }

        if (this.caseSensitive.containsKey(expression)) {
            targets.addAll(this.caseSensitive.get(expression));
        }

        this.patterns.forEach((k, v) -> {
            if (k.matcher(expression).matches()) {
                targets.addAll(v);
            }
        });

        return targets;
    }

    public void addConditionTarget(String isRegEx, String isCaseSensitive, String expression, List<TargetFacet> targets) {
        if ("true".equalsIgnoreCase(isRegEx)) {
            this.patterns.put(Pattern.compile(expression), targets);
        } else if ("true".equalsIgnoreCase(isCaseSensitive)) {
            this.caseSensitive.put(expression, targets);
        } else {
            this.caseLess.put(expression.toLowerCase(), targets);
        }
    }
}
