package eu.clarin.cmdi.vlo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class VloApiApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(VloApiApplication.class, args);        

    }

}
