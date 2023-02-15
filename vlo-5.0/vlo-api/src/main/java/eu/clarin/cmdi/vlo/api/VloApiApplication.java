package eu.clarin.cmdi.vlo.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class VloApiApplication {

    public static void main(String[] args) {
        log.info("VLO API: starting");
        SpringApplication.run(VloApiApplication.class, args);
        log.info("VLO API: started");
    }

}
