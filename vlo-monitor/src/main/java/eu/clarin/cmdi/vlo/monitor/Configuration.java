package eu.clarin.cmdi.vlo.monitor;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloConfigFactory;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    @Value("${vlo.monitor.config.url}")
    private String configLocation;

    @Bean
    public VloConfig vloConfig() throws IOException {
        return vloConfigFactory().newConfig();
    }

    @Bean
    public VloConfigFactory vloConfigFactory() throws MalformedURLException {
        if (configLocation == null) {
            return new DefaultVloConfigFactory();
        } else {
            return new XmlVloConfigFactory(URI.create(configLocation).toURL());
        }
    }

}
