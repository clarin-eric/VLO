package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.monitor.VloMonitorConfiguration;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
@Slf4j
public class VloMonitor {

    private final VloMonitorConfiguration config;
    private final IndexService indexService;
    private final IndexStateRepository repo;
    private final RulesService rulesService;
    private final IndexStateCompareService compareService;
    private final ReportingService reportingService;

    public VloMonitor(VloMonitorConfiguration config, IndexService indexService, IndexStateRepository repo, RulesService rules, IndexStateCompareService compareService, ReportingService reportingService) {
        this.config = config;
        this.indexService = indexService;
        this.repo = repo;
        this.rulesService = rules;
        this.compareService = compareService;
        this.reportingService = reportingService;
    }

    public void run() {
        log.info("VLO monitor run - {}", Calendar.getInstance().getTime());

        final IndexState newIndexState = newIndexState();
        logIndexStateStats("New state", newIndexState);

        log.info("Loading previous stats");
        final Optional<IndexState> previousIndexState = repo.findFirstByOrderByTimestampDesc();

        previousIndexState.ifPresentOrElse(
                (previous) -> {
                    logIndexStateStats("Previous state", previous);
                    compareStates(previous, newIndexState);
                },
                () -> log.info("No previous state, skipping comparison!"));

        log.info("Writing new stats");
        repo.save(newIndexState);

        pruneRepo();
        log.info("Done");
    }

    private IndexState newIndexState() {
        final IndexState newIndexState = new IndexState();
        newIndexState.setTimestamp(Calendar.getInstance().getTime());

        final Collection<String> ruleFields = rulesService.getAllFields();

        final List<FacetState> facetStates = ruleFields.stream()
                // get facet states for listed facets
                .map(this::getFacetStateStreamForFacet)
                // combine streams
                .flatMap(Function.identity())
                // collect into list
                .collect(Collectors.toList());
        newIndexState.setFacetStates(facetStates);
        newIndexState.setTotalRecordCount(indexService.getTotalRecordCount());
        return newIndexState;
    }

    private void compareStates(IndexState previousIndexState, final IndexState newIndexState) {
        log.info("Comparing old and new state");
        final Collection<MonitorReportItem> report = compareService.compare(previousIndexState, newIndexState);
        reportingService.report(report);
    }

    private Stream<FacetState> getFacetStateStreamForFacet(String facet) {
        return indexService.getValueCounts(facet)
                .entrySet()
                .stream()
                .map(pair -> new FacetState(facet, pair.getKey(), pair.getValue()));
    }

    private void logIndexStateStats(String name, IndexState indexState) {
        log.info("{}: {} ({} values)",
                name, indexState,
                Optional.ofNullable(indexState.getFacetStates()).map(List::size).orElse(0));
    }

    private void pruneRepo() {
        config.getPruneAfterDays()
                .filter(maxDays -> maxDays > 0)
                .ifPresentOrElse(
                        this::doPrune,
                        () -> {
                            log.info("No pruning of old states based on configuration setting '{}'", config.getPruneAfterDays());
                        });
    }

    /**
     * Prune any state older that specified age
     *
     * @param maxAgeInDays maximum age of state that shouldn't be pruned
     */
    private void doPrune(Integer maxAgeInDays) {
        // calculate max date from max number of days in the past        
        final Date pruneThreshold = DateUtils.addDays(new Date(), -maxAgeInDays);

        final List<IndexState> statesToPrune
                = ImmutableList.copyOf(repo.findOlderThan(pruneThreshold));

        log.info("Found {} old states to prune: {}{}", statesToPrune.size(),
                statesToPrune.stream().map(IndexState::toString).limit(10).toArray(),
                statesToPrune.size() > 10 ? " [...]" : ""
        );

        repo.deleteAll(statesToPrune);

        log.info("Deleted {} old states from the repository", statesToPrune.size());
    }
}
