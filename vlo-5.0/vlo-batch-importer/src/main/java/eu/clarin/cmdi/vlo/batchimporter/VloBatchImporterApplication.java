package eu.clarin.cmdi.vlo.batchimporter;

import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;

@SpringBootApplication
@EnableReactiveElasticsearchRepositories(basePackageClasses = VloRecordRepository.class)
public class VloBatchImporterApplication {

    public static void main(String[] args) {
        System.exit(
                SpringApplication.exit(
                        SpringApplication.run(VloBatchImporterApplication.class, args)));
    }

}
