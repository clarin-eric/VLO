package eu.clarin.cmdi.vlo.monitor;

import eu.clarin.cmdi.vlo.monitor.service.VloMonitor;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class MonitorApplication {

    @Inject
    private VloMonitor vloMonitor;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            log.info("Starting VLO monitor application");
            vloMonitor.run(null);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }

}
