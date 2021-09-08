package eu.clarin.cmdi.vlo.batchimporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

@SpringBootApplication(
        exclude = ElasticsearchDataAutoConfiguration.class)
public class VloBatchImporterApplication {

    public static void main(String[] args) {
        System.exit(
                SpringApplication.exit(
                        SpringApplication.run(VloBatchImporterApplication.class, args)));
    }

}
