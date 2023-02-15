package eu.clarin.cmdi.vlo.api;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class VloApiApplication {

    public static void main(String[] args) {
        log.info("VLO API: starting");
        final ConfigurableApplicationContext applicationContext = SpringApplication.run(VloApiApplication.class, args);
        log.info("VLO API: started with active profiles {}", Arrays.toString(applicationContext.getEnvironment().getActiveProfiles()));
    }

}
