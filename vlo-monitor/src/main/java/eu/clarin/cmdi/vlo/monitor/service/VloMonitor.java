package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.monitor.Rules;
import eu.clarin.cmdi.vlo.monitor.VloMonitorConfiguration;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
    private final IndexStateCompareService compareService;
    private final Rules rules;
    private Collection<String> fields;

    public VloMonitor(VloMonitorConfiguration config, IndexService indexService, IndexStateRepository repo, IndexStateCompareService compareService, Rules rules) {
        this.config = config;
        this.indexService = indexService;
        this.repo = repo;
        this.compareService = compareService;
        this.rules = rules;
    }

    @PostConstruct
    protected final void init() {
        fields = rules.getAllFields();
        log.debug("Fields: {}", fields);
    }

    public void run() {
        log.info("VLO monitor run - {}", Calendar.getInstance().getTime());

        final IndexState newIndexState = newIndexState();
        logIndexStateStats("New state", Optional.of(newIndexState));

        log.info("Loading previous stats");
        final Optional<IndexState> previousIndexState = repo.findFirstByOrderByTimestampDesc();
        logIndexStateStats("Previous state", previousIndexState);

        log.info("Comparing old and new state");
        final Collection<MonitorReportItem> report = compareService.compare(newIndexState, newIndexState, rules);
        logReport(report);

        log.info("Writing new stats");
        repo.save(newIndexState);

        pruneRepo();
        log.info("Done");
    }

    private IndexState newIndexState() {
        final IndexState newIndexState = new IndexState();
        newIndexState.setTimestamp(Calendar.getInstance().getTime());
        final List<FacetState> facetStates = fields.stream()
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

    private Stream<FacetState> getFacetStateStreamForFacet(String facet) {
        return indexService.getValueCounts(facet)
                .entrySet()
                .stream()
                .map(pair -> new FacetState(facet, pair.getKey(), pair.getValue()));
    }

    private void logIndexStateStats(String name, Optional<IndexState> indexState) {
        log.info("{}: {} ({} values)",
                name, indexState.map(IndexState::toString).orElse("EMPTY"),
                indexState.flatMap(i -> Optional.ofNullable(i.getFacetStates())).map(List::size).orElse(0));
    }

    private void logReport(Collection<MonitorReportItem> report) {
        if (report.isEmpty()) {
            log.info("No significant differences in comparison");
        } else {
            report.forEach(item -> {
                final String message = item.toString();
                switch (item.getLevel()) {
                    case WARN:
                        log.warn(message);
                        break;
                    case ERROR:
                        log.error(message);
                        break;
                    default:
                        log.info(message);
                        break;
                }
            });
        }
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

    private void doPrune(Integer maxDays) {
        final Calendar calendar = Calendar.getInstance();
        // set max date based on max number of days in the past
        calendar.add(Calendar.HOUR, -24 * maxDays);
        
        final List<IndexState> statesToPrune
                = ImmutableList.copyOf(repo.findOlderThan(calendar.getTime()));

        log.info("Found {} old states to prune", statesToPrune.size());

        repo.deleteAll(statesToPrune);

        log.info("Deleted {} old states from the repository", statesToPrune.size());
    }
}
