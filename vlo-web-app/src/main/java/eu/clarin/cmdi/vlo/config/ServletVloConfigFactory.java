/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Vlo configuration factory that reads the configuration from the location
 * specified by the context parameter
 * <em>eu.carlin.cmdi.vlo.config.location</em> via {@link XmlVloConfigFactory}
 * or falls back to the internal default configuration via
 * {@link DefaultVloConfigFactory}
 *
 * <p>
 * It also supports the following overrides:
 * <ul>
 * <li><em>eu.carlin.cmdi.vlo.solr.serverUrl</em> - SOLR server URL to connect
 * to</li>
 * </ul>
 * </p>
 *
 * @author twagoo
 * @see VloConfig
 */
public class ServletVloConfigFactory implements VloConfigFactory {

    private final static Logger logger = LoggerFactory.getLogger(ServletVloConfigFactory.class);

    @Value("${eu.carlin.cmdi.vlo.config.location:}")
    private String configLocation;

    @Value("${eu.carlin.cmdi.vlo.solr.serverUrl:}")
    private String solrServerUrl;

    @Override
    public VloConfig newConfig() throws IOException {
        final VloConfigFactory factory = getFactory();
        final VloConfig configuration = factory.newConfig();
        applyProperties(configuration);
        return configuration;
    }

    private VloConfigFactory getFactory() throws MalformedURLException {
        if (configLocation == null || configLocation.isEmpty()) {
            logger.info("Using default VLO configuration");
            return new DefaultVloConfigFactory();
        } else {
            logger.info("Reading VLO configuration from {}", configLocation);
            return new XmlVloConfigFactory(new File(configLocation).toURI().toURL());
        }
    }

    private void applyProperties(VloConfig configuration) {
        if (!(solrServerUrl == null || solrServerUrl.isEmpty())) {
            logger.info("Overriding SOLR server URL with location from context parameter: {}", solrServerUrl);
            configuration.setSolrUrl(solrServerUrl);
        }
    }

}
