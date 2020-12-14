package eu.clarin.cmdi.vlo.monitor;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.Repository;

@SpringBootApplication
@Slf4j
public class MonitorApplication {

    private final Collection<String> facets = ImmutableList.of("collection", "_oaiEndpointURI");

    @Inject
    private IndexService indexService;
    
    @Inject
    private Repository<IndexState, Long> repo;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            log.info("VLO monitor run - {}", Calendar.getInstance().getTime());

            facets.forEach(f -> {
                final Map<String, Integer> valueCounts = indexService.getValueCounts(f);
                log.debug("Facet {} - values {}", f, valueCounts);
            });

            //TODO: Collect current stats
            //TODO: Load previous stats
            //TODO: Compare to previous stats
            //TODO: Write new stats
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }

}
