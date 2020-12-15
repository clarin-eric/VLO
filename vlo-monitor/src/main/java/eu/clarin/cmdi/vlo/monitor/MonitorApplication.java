package eu.clarin.cmdi.vlo.monitor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;

@SpringBootApplication
@Slf4j
public class MonitorApplication {
    
    private final Collection<String> facets = ImmutableList.of("collection", "_oaiEndpointURI");
    
    @Inject
    private IndexService indexService;
    
    @Inject
    private CrudRepository<IndexState, Long> repo;
    
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            
            log.info("VLO monitor run - {}", Calendar.getInstance().getTime());
            
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

            //TODO: Load previous stats
            log.info("Loading previous stats");
            Streams.stream(repo.findAll())
                    .forEach(i -> {
                        log.info("Found state from {}", i.getTimestamp());
                    });
            //TODO: Compare to previous stats
            
            //TODO: Write new stats
            log.info("Writing new stats");
            repo.save(newIndexState);
            log.info("Done");
        };
    }
    
    public Stream<FacetState> getFacetStateStreamForFacet(String facet) {
        return indexService.getValueCounts(facet)
                .entrySet()
                .stream()
                .map(pair -> new FacetState(facet, pair.getKey(), pair.getValue()));
    }
    
    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }
    
}
