package eu.clarin.cmdi.vlo.monitor.model;

import java.util.Optional;
import org.slf4j.event.Level;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public abstract class Rule {
    
    protected final RuleScope scope;
    protected final String field;
    protected final Level level;

    public enum RuleScope {
        FIELD_VALUE_COUNT,
        TOTAL_RECORD_COUNT
    }

    public Rule(RuleScope scope, String field, Level level) {
        this.scope = scope;
        this.field = field;
        this.level = level;
    }

    /**
     *
     * @param oldCount
     * @param newCount
     * @return whether rule applies
     */
    public abstract boolean evaluate(Long oldCount, Long newCount);

    public RuleScope getScope() {
        return scope;
    }

    public String getField() {
        return field;
    }

    public Level getLevel() {
        return level;
    }

    public static Rule create(RuleScope scope, Level level, Optional<String> field, String definition) {
        if (definition.endsWith("%")) {
            //percentage indicates a thresholdRatio based rule
            final double decreaseRatioThreshold = Double.parseDouble(definition.substring(0, definition.indexOf('%')).trim());
            return new RatioDecreaseRule(scope, field.orElse(null), level, decreaseRatioThreshold);
        } else {
            //assume it's a number that can be parsed as a Long - interpret as an absolute decrease rule
            final long decreaseThreshold = Long.parseLong(definition.trim());
            return new AbsoluteDecreaseRule(scope, field.orElse(null), level, decreaseThreshold);
        }
    }


    public static class RatioDecreaseRule extends Rule {

        final double thresholdRatio;

        public RatioDecreaseRule(RuleScope scope, String field, Level level, double thresholdRatio) {
            super(scope, field, level);
            this.thresholdRatio = .01 * thresholdRatio;
        }

        @Override
        public boolean evaluate(Long oldCount, Long newCount) {
            return (oldCount - newCount) >= (thresholdRatio * oldCount);
        }

        public double getThresholdRatio() {
            return thresholdRatio;
        }

        @Override
        public String toString() {
            return String.format("Ratio decrease rule: applies if decrease is %f%% or more", 100 * thresholdRatio);
        }
    }

    public static class AbsoluteDecreaseRule extends Rule {

        final long thresholdDiff;

        public AbsoluteDecreaseRule(RuleScope scope, String field, Level level, Long thresholdDiff) {
            super(scope, field, level);
            this.thresholdDiff = thresholdDiff;
        }

        @Override
        public boolean evaluate(Long oldCount, Long newCount) {
            return (oldCount - newCount) >= thresholdDiff;
        }

        public long getThresholdDiff() {
            return thresholdDiff;
        }

        @Override
        public String toString() {
            return String.format("Absolute decrease rule: applies if decrease is %d or more", thresholdDiff);
        }

    }
    
}
