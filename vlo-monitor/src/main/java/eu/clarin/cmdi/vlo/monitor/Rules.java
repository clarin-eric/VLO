package eu.clarin.cmdi.vlo.monitor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Component
public class Rules {

    private final RulesConfig config;

    public Rules(RulesConfig rulesConfig) {
        this.config = rulesConfig;
    }

    public Collection<String> getAllFields() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        if (config.getFieldValuesDecreaseWarning() != null) {
            builder.addAll(config.getFieldValuesDecreaseWarning().keySet());
        }
        if (config.getFieldValuesDecreaseError() != null) {
            builder.addAll(config.getFieldValuesDecreaseError().keySet());
        }
        return builder.build();
    }

    public Map<String, List<Rule>> getFieldRules() {
        // combine 'warning' and 'error' rules for all facets
        return Streams.concat(
                createRules(Level.WARN, Optional.ofNullable(config.getFieldValuesDecreaseWarning())),
                createRules(Level.ERROR, Optional.ofNullable(config.getFieldValuesDecreaseError()))
        ).collect(Collectors.groupingBy(Rule::getField));
    }

    public Stream<Rule> createRules(Level level, Optional<Map<String, String>> rulesMap) {
        //we want to 'flatten' the definitions to a stream of rules
        return rulesMap
                //optional as a stream
                .stream()
                //turn map into a stream of rules (we get an empty stream if the rulesMap is empty)
                .flatMap(m -> {
                    return m.entrySet()
                            .stream()
                            .map(entrySet -> {
                                //key = field
                                //value = 'definition' e.g. '100' or '25%'
                                return Rule.create(level, entrySet.getKey(), entrySet.getValue());
                            });
                });
    }

    public RulesConfig getConfig() {
        return config;
    }

    public static abstract class Rule {

        protected final String field;
        protected final Level level;

        public Rule(String field, Level level) {
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

        public String getField() {
            return field;
        }

        public Level getLevel() {
            return level;
        }

        public static Rule create(Level level, String field, String definition) {
            if (definition.endsWith("%")) {
                //percentage indicates a thresholdRatio based rule
                final double decreaseRatioThreshold = Double.parseDouble(definition.substring(0, definition.indexOf('%')).trim());
                return new RatioDecreaseRule(field, level, decreaseRatioThreshold);
            } else {
                //assume it's a number that can be parsed as a Long - interpret as an absolute decrease rule
                final long decreaseThreshold = Long.parseLong(definition.trim());
                return new AbsoluteDecreaseRule(field, level, decreaseThreshold);
            }
        }

    }

    public static class RatioDecreaseRule extends Rule {

        private final double thresholdRatio;

        public RatioDecreaseRule(String field, Level level, double thresholdRatio) {
            super(field, level);
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

        private final long thresholdDiff;

        public AbsoluteDecreaseRule(String field, Level level, Long thresholdDiff) {
            super(field, level);
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
