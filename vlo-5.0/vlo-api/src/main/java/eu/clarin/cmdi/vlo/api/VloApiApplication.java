package eu.clarin.cmdi.vlo.api;

import eu.clarin.cmdi.vlo.api.data.VloRecordRepository;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@SpringBootApplication
@EnableReactiveElasticsearchRepositories
@Slf4j
public class VloApiApplication {

    public static void main(String[] args) {
        log.info("VLO API: starting");
        SpringApplication.run(VloApiApplication.class, args);
        log.info("VLO API: done");
    }

    @Bean
    public boolean createTestRecord(VloRecordRepository repo) {
        if (repo.findById("999").isEmpty()) {
            repo.save(VloRecord.builder().id("999").name("Test record").build());
        }
        return true;
    }

}
