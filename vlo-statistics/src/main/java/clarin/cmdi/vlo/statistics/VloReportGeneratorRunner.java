/*
 * Copyright (C) 2016 CLARIN
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
package clarin.cmdi.vlo.statistics;

import clarin.cmdi.vlo.statistics.reporting.StatsdReporter;
import clarin.cmdi.vlo.statistics.reporting.XmlReportWriter;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import javax.xml.bind.JAXBException;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class VloReportGeneratorRunner {

    private final static Logger logger = LoggerFactory.getLogger(VloReportGeneratorRunner.class);

    public static void main(String[] args) throws MalformedURLException, IOException, SolrServerException, JAXBException {
        final Properties properties = loadProperties(args);

        final File configLocation = new File(properties.getProperty("vlo.config.file", "VloConfig.xml"));
        if (!configLocation.exists()) {
            logger.error("Configuration file {} does not exist", configLocation);
            System.exit(1);
        }

        // load config
        logger.info("Loading configuration from {}", configLocation);
        final XmlVloConfigFactory xmlVloConfigFactory
                = new XmlVloConfigFactory(configLocation.toURI().toURL());
        final VloConfig vloConfig = xmlVloConfigFactory.newConfig();

        // instantiate generator
        final VloReportGenerator vloReportGenerator = new VloReportGenerator(vloConfig);
        // complete configuration
        applyConfigurationOptions(vloReportGenerator, properties);

        // start report generator
        logger.info("Gathering statistics...");
        vloReportGenerator.run();
    }

    private static Properties loadProperties(String[] args) throws IOException {
        final String propsFile;
        if (args.length >= 1) {
            propsFile = args[0];
        } else {
            logger.warn("No configuration file provided. Trying default location.");
            propsFile = "configuration.properties";
        }
        final File propertiesLocation = new File(propsFile);
        if (!propertiesLocation.exists()) {
            logger.error("Configuration file {} does not exist", propertiesLocation);
            System.exit(1);
        }
        final Properties properties = new Properties();
        properties.load(new FileReader(args[0]));
        return properties;
    }

    private static void applyConfigurationOptions(final VloReportGenerator vloReportGenerator, final Properties properties) throws JAXBException {
        //output file
        final String outputFileBase = properties.getProperty("report.xml.file.name");
        if (outputFileBase != null) {
            // create full output filename
            final StringBuilder outputFileNameBuilder = new StringBuilder(outputFileBase);

            final String dateFormatString = properties.getProperty("report.xml.file.dateformat");
            if (dateFormatString != null) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
                final String datePart = dateFormat.format(Calendar.getInstance().getTime());
                outputFileNameBuilder.append(datePart);
            }

            // append extension
            outputFileNameBuilder.append(".xml");

            // check if we will be able to write to this file
            final File xmlReportTarget = new File(outputFileNameBuilder.toString());
            if (xmlReportTarget.exists() && (xmlReportTarget.isDirectory() || !xmlReportTarget.canWrite())) {
                logger.error("Cannot write to output file {}", xmlReportTarget);
                System.exit(1);
            } else {
                logger.info("An XML report will be generated in {}", xmlReportTarget);
                vloReportGenerator.getResultHandlers().add(new XmlReportWriter(xmlReportTarget));
            }
        }

        //statsd
        final String statsdHost = properties.getProperty("report.statsd.server.host");
        if (statsdHost != null) {
            final String statsdPort = properties.getProperty("report.statsd.server.port");
            final String statsdPrefix = properties.getProperty("report.statsd.prefix");
            final String statsdHistoryFile = properties.getProperty("report.statsd.historyFile");
            if (statsdPort == null || statsdPrefix == null || statsdHistoryFile == null) {
                logger.error("One or more statsd properties have not been configured correctly");
                System.exit(1);
            }

            try {
                final int portNumber = Integer.parseInt(statsdPort);
                logger.info("Statistics will be sent to {}:{}", statsdHost, portNumber);

                final StatsdReporter statsdReporter = new StatsdReporter(statsdPrefix, statsdHost, portNumber, new File(statsdHistoryFile));

                // we may want to use a no-op client
                if ("true".equalsIgnoreCase(properties.getProperty("report.statsd.noop"))) {
                    logger.warn("Using a no-op statsd client!");
                    statsdReporter.setNoop(true);
                }

                vloReportGenerator.getResultHandlers().add(statsdReporter);
            } catch (NumberFormatException ex) {
                logger.error("Invalid statsd port number: {}", statsdPort);
                logger.debug("Invalid statsd port number", ex);
                System.exit(1);
            }
        }
    }
}
