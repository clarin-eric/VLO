package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
@Slf4j
public class VloMonitor {

    private final Collection<String> facets = ImmutableList.of("collection", "_oaiEndpointURI");

    @Inject
    private IndexService indexService;

    @Inject
    private IndexStateRepository repo;

    public void run() {

        log.info("VLO monitor run - {}", Calendar.getInstance().getTime());

        final IndexState newIndexState = newIndexState();

        log.info("Loading previous stats");
        final Optional<IndexState> previousIndexState = repo.findFirstByOrderByTimestampDesc();
        log.info("Latest state: {}", previousIndexState.map(i -> i.getTimestamp().toString()).orElse("null"));

        //TODO: Compare to previous stats
        log.info("Writing new stats");
        repo.save(newIndexState);

        //TODO: Clean up old stats?
        log.info("Done");
    }

    private IndexState newIndexState() {
        final IndexState newIndexState = new IndexState();
        newIndexState.setTimestamp(Calendar.getInstance().getTime());
        final List<FacetState> facetStates = facets.stream()
                // get facet states for listed facets
                .map(this::getFacetStateStreamForFacet)
                // combine streams
                .flatMap(Function.identity())
                // collect into list
                .collect(Collectors.toList());
        newIndexState.setFacetStates(facetStates);
        return newIndexState;
    }

    private Stream<FacetState> getFacetStateStreamForFacet(String facet) {
        return indexService.getValueCounts(facet)
                .entrySet()
                .stream()
                .map(pair -> new FacetState(facet, pair.getKey(), pair.getValue()));
    }
}
