package eu.clarin.cmdi.vlo.monitor.service;

import eu.clarin.cmdi.vlo.monitor.model.Rule;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import static eu.clarin.cmdi.vlo.monitor.model.Rule.RuleScope.FIELD_VALUE_COUNT;
import static eu.clarin.cmdi.vlo.monitor.model.Rule.RuleScope.TOTAL_RECORD_COUNT;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
public class IndexStateCompareServiceImpl implements IndexStateCompareService {

    private final RulesService rulesService;

    public IndexStateCompareServiceImpl(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public Collection<MonitorReportItem> compare(IndexState oldState, IndexState newState) {
        final IndexStateComparison comparison = new IndexStateComparison(oldState, newState);
        return comparison.compare();
    }

    private class IndexStateComparison {

        private final IndexState oldState;
        private final IndexState newState;

        private Map<String, List<FacetState>> oldStateByField;
        private Map<String, List<FacetState>> newStateByField;

        public IndexStateComparison(IndexState oldState, IndexState newState) {
            this.oldState = oldState;
            this.newState = newState;
        }

        public Collection<MonitorReportItem> compare() {
            oldStateByField = oldState.getFacetStates().stream()
                    .collect(Collectors.groupingBy(FacetState::getFacet));
            newStateByField = newState.getFacetStates().stream()
                    .collect(Collectors.groupingBy(FacetState::getFacet));

            return rulesService.getRules()
                    .stream()
                    .flatMap(this::evaluate)
                    .collect(Collectors.toList());
        }

        private Stream<MonitorReportItem> evaluate(Rule rule) {
            switch (rule.getScope()) {
                case FIELD_VALUE_COUNT:
                    return evaluateFieldRule(rule);
                case TOTAL_RECORD_COUNT:
                    return evaluateRecordCountRule(rule);
                default:
                    throw new RuntimeException("Unsupported rule scope: " + rule.getScope());
            }
        }

        private Stream<MonitorReportItem> evaluateRecordCountRule(Rule rule) {
            assert (rule.getScope() == TOTAL_RECORD_COUNT);

            final Long oldTotalRecordCount = oldState.getTotalRecordCount();
            final Long newTotalRecordCount = newState.getTotalRecordCount();

            if (rule.evaluate(oldTotalRecordCount, newTotalRecordCount)) {
                return Stream.of(new MonitorReportItem(
                        rule,
                        Optional.empty(),
                        String.format("{%d -> %d} Total record count decrease above threshold. Triggered by rule: [%s]", oldTotalRecordCount, newTotalRecordCount, rule.toString())));
            } else {
                return Stream.empty();
            }
        }

        private Stream<MonitorReportItem> evaluateFieldRule(Rule rule) {
            assert (rule.getScope() == FIELD_VALUE_COUNT);

            // Rule applies to a single field; get counts for field from old and new index
            final String field = rule.getField();
            final List<FacetState> oldStateCounts = oldStateByField.getOrDefault(field, Collections.emptyList());
            final List<FacetState> newStateCounts = newStateByField.getOrDefault(field, Collections.emptyList());

            // Iterate over old state counts for field
            return oldStateCounts.stream()
                    .flatMap(oldStateCount -> {
                        final String fieldValue = oldStateCount.getVal();

                        final Long oldCount = oldStateCount.getCount();
                        // determine count for same facet+value pair in new state
                        final Long newCount = newStateCounts.stream()
                                // get FacetState for same value in new index state
                                .filter(s -> fieldValue.equals(s.getVal())).findFirst()
                                // Get count for value in Facet
                                .map(FacetState::getCount)
                                // If new state not found for this value, we treat that as 0
                                .orElse(0L);

                        // Evaluate rule for the counts
                        if (rule.evaluate(oldCount, newCount)) {
                            final MonitorReportItem reportItem
                                    = new MonitorReportItem(
                                            rule,
                                            Optional.of(fieldValue),
                                            String.format("{%d -> %d} Triggered by rule: [%s]", oldCount, newCount, rule.toString()));
                            return Stream.of(reportItem);
                        } else {
                            return Stream.empty();
                        }
                    });
        }
    }

}
