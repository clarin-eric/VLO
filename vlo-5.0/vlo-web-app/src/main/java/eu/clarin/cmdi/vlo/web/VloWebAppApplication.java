package eu.clarin.cmdi.vlo.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;

@SpringBootApplication(exclude = {ElasticsearchDataAutoConfiguration.class})
public class VloWebAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(VloWebAppApplication.class, args);
	}

}
