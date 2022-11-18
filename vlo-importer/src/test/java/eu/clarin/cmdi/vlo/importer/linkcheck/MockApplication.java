package eu.clarin.cmdi.vlo.importer.linkcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;



/**
 * @author WolfgangWalter Sauer (wowasa)
 * 
 * the purpose of the class is only to have a SpringBootApplication annotated class, which is necessary to execute all the automatic stuff
 * like loading the configuration, executing the schema.sql, etc.
 *
 */
@SpringBootApplication(scanBasePackages = {"eu.clarin.linkchecker.persistence", "eu.clarin.cmdi.vlo.importer.linkcheck"})
@EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
@EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
public class MockApplication {
   

	public static void main(String[] args) {
		SpringApplication.run(MockApplication.class, args);
	}
}
