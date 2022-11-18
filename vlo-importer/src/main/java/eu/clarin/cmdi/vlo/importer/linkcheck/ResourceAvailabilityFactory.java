/*
 * Copyright (C) 2022 CLARIN
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
package eu.clarin.cmdi.vlo.importer.linkcheck;

import com.google.common.base.Strings;
import eu.clarin.cmdi.rasa.helpers.RasaFactory;
import eu.clarin.cmdi.rasa.helpers.impl.RasaFactoryBuilderImpl;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ResourceAvailabilityFactory {

    /**
     * Logging
     */
    protected final static Logger LOG = LoggerFactory.getLogger(ResourceAvailabilityFactory.class);
    /**
     * RASA database driver class
     */
    private static final String RASA_JDBC_DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";

    public static ResourceAvailabilityStatusChecker createDefaultResourceAvailabilityStatusChecker(final VloConfig config) {
        final String rasaDbUri = config.getLinkCheckerDbConnectionString(); //jdbc:mysql://localhost:3306/linkchecker
        final String rasaDbUser = config.getLinkCheckerDbUser(); //linkchecker
        final String rasaDbPassword = config.getLinkCheckerDbPassword(); //linkchecker
        final int rasaDbPoolsize = config.getLinkCheckerDbPoolsize();
        final Duration checkAgeThreshold = Duration.ofDays(config.getLinkCheckerMaxDaysSinceChecked());
        if (!Strings.isNullOrEmpty(rasaDbUri)) {
            try {
                final RasaResourceAvailabilityStatusChecker checker = newRasaChecker(rasaDbUri, rasaDbUser, rasaDbPassword, rasaDbPoolsize, checkAgeThreshold);
                if (testChecker(checker)) {
                    return checker;
                }
            } catch (Exception ex) {
                LOG.error("Error while initialising resource availability checker", ex);
            }
            LOG.warn("Resource availability checker initialisation and/or test FAILED. Installing a NOOP availability checker. Availability status will NOT be checked!");
            return new NoopResourceAvailabilityStatusChecker();
        } else {
            LOG.warn("No mysql configuration - installing a NOOP availability checker. Availability status will NOT be checked!");
            return new NoopResourceAvailabilityStatusChecker();
        }
    }

    private static RasaResourceAvailabilityStatusChecker newRasaChecker(final String rasaDbUri, final String rasaDbUser, final String rasaDbPassword, final int rasaDbPoolsize, final Duration checkAgeThreshold) {
        LOG.debug("Connecting to RASA database '{}' for link checker information", rasaDbUri);
        final Properties rasaProperties = new Properties();
        rasaProperties.setProperty("driverClassName", RASA_JDBC_DRIVER_CLASS_NAME);
        rasaProperties.setProperty("jdbcUrl", rasaDbUri);
        rasaProperties.setProperty("username", rasaDbUser);
        rasaProperties.setProperty("password", rasaDbPassword);
        rasaProperties.setProperty("maximumPoolSize", String.valueOf(rasaDbPoolsize));
        final RasaFactory factory = new RasaFactoryBuilderImpl().getRasaFactory(rasaProperties);
        final CheckedLinkResource checkedLinkResource = factory.getCheckedLinkResource();
        final RasaResourceAvailabilityStatusChecker checker = new RasaResourceAvailabilityStatusChecker(checkedLinkResource, new RasaResourceAvailabilityStatusCheckerConfiguration(checkAgeThreshold)) {
            @Override
            public void onClose() throws IOException {
                RasaResourceAvailabilityStatusChecker.logger.info("Asking resource availability checker factory to tear down");
                factory.tearDown();
            }

            @Override
            public void writeStatusSummary(Writer writer) throws IOException {
                factory.writeStatusSummary(writer);
            }
        };
        return checker;
    }

    private static boolean testChecker(RasaResourceAvailabilityStatusChecker checker) {
        LOG.debug("Carrying out functional check of RASA checker...");
        final String testUrl = "https://www.clarin.eu";
        try {
            checker.getLinkStatusForRefs(Stream.of(testUrl));
            return true;
        } catch (IOException ex) {
            LOG.error("Failed to carry out test check for {}", testUrl, ex);
            return false;
        }
    }

}
