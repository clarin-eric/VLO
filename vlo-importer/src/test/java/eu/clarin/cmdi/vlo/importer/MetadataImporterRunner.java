/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class MetadataImporterRunner {

    protected final static Logger LOG = LoggerFactory.getLogger(MetadataImporterRunner.class);

    /**
     * @param args
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void main(String[] args) throws MalformedURLException, IOException {

        // path to the configuration file
        String configFile = null;

        // use the Apache cli framework for getting command line parameters
        Options options = new Options();

        // Data root list passed from command line with -l option
        String cldrList = null;

        /**
         * Add a "c" option, the option indicating the specification of an XML
         * configuration file
         *
         * "l" option - to specify which data roots (from config file) to import
         * imports all by default
         */
        options.addOption("c", true, "-c <file> : use parameters specified in <file>");
        options.addOption("l", true, "-l <dataroot> [ ' ' <dataroot> ]* :  space separated list of dataroots to be processed.\n"
                + "If dataroot is not specified in config file it will be ignored.");
        options.getOption("l").setOptionalArg(true);

        CommandLineParser parser = new PosixParser();

        try {
            // parse the command line arguments
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("c")) {

                // the "c" option was specified, now get its value
                configFile = cmd.getOptionValue("c");
            }

            if (cmd.hasOption("l")) {
                cldrList = cmd.getOptionValue("l");
            }

        } catch (org.apache.commons.cli.ParseException ex) {

            /**
             * Caught an exception caused by command line parsing. Try to get
             * the name of the configuration file by querying the system
             * property.
             */
            String message = "Command line parsing failed. " + ex.getMessage();
            LOG.error(message);
            System.err.println(message);
        }

        if (configFile == null) {

            String message;

            message = "Could not get config file name via the command line, trying the system properties.";
            LOG.info(message);

            String key;

            key = "configFile";
            configFile = System.getProperty(key);
        }

        if (configFile == null) {

            String message;

            message = "Could not get filename as system property either - stopping.";
            LOG.error(message);
        } else {
            // read the configuration from the externally supplied file
            final URL configUrl;
            if (configFile.startsWith("file:")) {
                configUrl = new URL(configFile);
            } else {
                configUrl = new File(configFile).toURI().toURL();
            }
            System.out.println("Reading configuration from " + configUrl.toString());
            LOG.info("Reading configuration from " + configUrl.toString());
            final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(configUrl);
            MetadataImporter.config = configFactory.newConfig();
            MetadataImporter.languageCodeUtils = new LanguageCodeUtils(MetadataImporter.config);

            // optionally, modify the configuration here
            // create and start the importer
            MetadataImporter importer = new MetadataImporter(cldrList);
            importer.startImport();

            // finished importing
            if (MetadataImporter.config.printMapping()) {
                File file = new File("xsdMapping.txt");
                FacetMappingFactory.printMapping(file);
                LOG.info("Printed facetMapping in " + file);
            }
        }
    }
}
