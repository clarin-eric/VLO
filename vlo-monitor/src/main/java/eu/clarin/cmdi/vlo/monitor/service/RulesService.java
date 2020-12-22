package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.monitor.RulesConfig;
import eu.clarin.cmdi.vlo.monitor.model.Rule;
import eu.clarin.cmdi.vlo.monitor.model.Rule.RuleScope;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
public class RulesService {

    private final RulesConfig config;

    public RulesService(RulesConfig rulesConfig) {
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

    public Collection<Rule> getRules() {
        // combine 'warning' and 'error' rules for all facets
        return Streams.concat(
                createFieldRules(Level.WARN, Optional.ofNullable(config.getFieldValuesDecreaseWarning())),
                createFieldRules(Level.ERROR, Optional.ofNullable(config.getFieldValuesDecreaseError())),
                createTotalCountRules(Level.WARN, config.getTotalRecordsDecreaseWarning()),
                createTotalCountRules(Level.ERROR, config.getTotalRecordsDecreaseError())
        ).collect(Collectors.toList());
    }

    public Stream<Rule> createFieldRules(Level level, Optional<Map<String, String>> rulesMap) {
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
                                return Rule.create(RuleScope.FIELD_VALUE_COUNT, level, Optional.of(entrySet.getKey()), entrySet.getValue());
                            });
                });
    }

    public Stream<Rule> createTotalCountRules(Level level, String totalRecordsDecrease) {
        if (!ObjectUtils.isEmpty(totalRecordsDecrease)) {
            return Stream.of(Rule.create(RuleScope.TOTAL_RECORD_COUNT, level, Optional.empty(), totalRecordsDecrease));
        } else {
            return Stream.empty();
        }
    }

    public RulesConfig getConfig() {
        return config;
    }
}
