package eu.clarin.cmdi.vlo.monitor;

import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MonitorApplication {

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

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
