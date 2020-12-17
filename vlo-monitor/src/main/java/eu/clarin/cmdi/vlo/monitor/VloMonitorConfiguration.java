package eu.clarin.cmdi.vlo.monitor;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloConfigFactory;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Configuration
@Slf4j
public class VloMonitorConfiguration {

    @Value("${vlo.monitor.config.url:}")
    private String configLocation;

    @Bean
    public VloConfig vloConfig() throws IOException {
        return vloConfigFactory().newConfig();
    }

    @Bean
    public VloConfigFactory vloConfigFactory() throws MalformedURLException {
        if (ObjectUtils.isEmpty(configLocation)) {
            log.warn("No log file URL configured. Falling back to default VLO configuration!");
            return new DefaultVloConfigFactory();
        } else {
            return new XmlVloConfigFactory(URI.create(configLocation).toURL());
        }
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

}
