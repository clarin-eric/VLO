package eu.clarin.cmdi.vlo.monitor;

import org.springframework.context.annotation.Bean;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@org.springframework.context.annotation.Configuration
public class Configuration {
    
    @Bean
    public IndexService indexService() {
        return new IndexServiceImpl();
    }
    
}
