package eu.clarin.cmdi.vlo.monitor.service;

import eu.clarin.cmdi.vlo.monitor.Rules;
import eu.clarin.cmdi.vlo.monitor.Rules.Rule;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
public class IndexStateCompareServiceImpl implements IndexStateCompareService {

    @Override
    public Collection<MonitorReportItem> compare(IndexState oldState, IndexState newState, Rules rules) {
        final IndexStateComparison comparison = new IndexStateComparison(oldState, newState, rules);
        return comparison.compare();
    }

    static class IndexStateComparison {

        private final IndexState oldState;
        private final IndexState newState;
        private final Rules rules;

        private Map<String, List<FacetState>> oldStateByField;
        private Map<String, List<FacetState>> newStateByField;

        public IndexStateComparison(IndexState oldState, IndexState newState, Rules rules) {
            this.oldState = oldState;
            this.newState = newState;
            this.rules = rules;
        }

        public Collection<MonitorReportItem> compare() {
            oldStateByField = oldState.getFacetStates().stream()
                    .collect(Collectors.groupingBy(FacetState::getFacet));
            newStateByField = newState.getFacetStates().stream()
                    .collect(Collectors.groupingBy(FacetState::getFacet));

            return rules.getRules()
                    .stream()
                    .flatMap(this::evaluate)
                    .collect(Collectors.toList());
        }

        private Stream<MonitorReportItem> evaluate(Rule rule) {
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
                                            rule.getLevel(),
                                            Optional.of(field),
                                            Optional.of(fieldValue),
                                            rule.toString());
                            return Stream.of(reportItem);
                        } else {
                            return Stream.empty();
                        }
                    });
        }
    }

}
