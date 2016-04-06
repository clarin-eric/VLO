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

import clarin.cmdi.vlo.statistics.model.VloReport;
import clarin.cmdi.vlo.statistics.model.VloReport.CollectionCount;
import clarin.cmdi.vlo.statistics.model.VloReport.Facet;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class VloReportGenerator {

    private final static Logger logger = LoggerFactory.getLogger(VloReportGenerator.class);

    private final VloConfig config;
    private final HttpSolrServer solrServer;
    private File xmlOutputFile;
    private int statsdPort;
    private String statsdHost;
    private String statsdPrefix;

    public VloReportGenerator(VloConfig config) {
        this.config = config;
        this.solrServer = new HttpSolrServer(config.getSolrUrl());
    }

    public void run() throws SolrServerException, IOException, JAXBException {
        // Report object
        final VloReport report = new VloReport();

        try {
            // Gather statistics
            report.setRecordCount(getRecordCount());
            report.setCollections(obtainCollectionCounts());
            report.setFacets(obtainFacetStats());
        } finally {
            solrServer.shutdown();
        }

        if (xmlOutputFile != null) {
            // Write report
            marshallReport(report);
        }
        if(statsdHost != null) {
            sendToStatsd(report);
        }
    }

    private long getRecordCount() throws SolrServerException {
        final SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(0);
        final QueryResponse result = solrServer.query(query);
        return result.getResults().getNumFound();
    }

    private List<CollectionCount> obtainCollectionCounts() throws SolrServerException {
        final SolrQuery query = new SolrQuery();
        query.setRows(0);
        query.setFacet(true);
        query.addFacetField(FacetConstants.FIELD_COLLECTION);
        query.setFacetLimit(-1);

        final QueryResponse result = solrServer.query(query);
        final FacetField collectionField = result.getFacetField(FacetConstants.FIELD_COLLECTION);
        logger.debug("Collection field: {}", collectionField.getValues());

        final List<CollectionCount> counts
                = collectionField.getValues().stream().map((count) -> {
                    CollectionCount collectionCount = new CollectionCount();
                    collectionCount.setCollection(count.getName());
                    collectionCount.setCount(count.getCount());
                    return collectionCount;
                }).collect(Collectors.toList());
        return counts;
    }

    private List<Facet> obtainFacetStats() throws SolrServerException {
        final SolrQuery query = new SolrQuery();
        query.setRows(0);
        query.setFacet(true);
        config.getAllFacetFields().forEach((field) -> {
            query.addFacetField(field);
        });
        query.setFacetLimit(-1);

        final QueryResponse result = solrServer.query(query);
        final List<FacetField> facetFields = result.getFacetFields();

        final List<Facet> facets
                = facetFields.stream().map((field) -> {
                    final Facet facet = new Facet();
                    facet.setName(field.getName());
                    facet.setValueCount(field.getValueCount());
                    return facet;
                }).collect(Collectors.toList());
        return facets;
    }

    private void marshallReport(VloReport report) throws JAXBException {
        // Prepare marshaller
        final JAXBContext jc = JAXBContext.newInstance(VloReport.class);
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Write to target
        logger.info("Writing report to {}", xmlOutputFile);
        marshaller.marshal(report, xmlOutputFile);
    }

    private void sendToStatsd(VloReport report) {
        logger.info("Sending reports to statsd server {}:{} with prefix '{}'", statsdHost, statsdPort, statsdPrefix);
        //TODO
    }

    public void setXmlOutputFile(File xmlOutputFile) {
        this.xmlOutputFile = xmlOutputFile;
    }

    private void setStatsdHost(String statsdHost) {
        this.statsdHost = statsdHost;
    }

    private void setStatsdPort(int port) {
        this.statsdPort = port;
    }

    private void setStatsdPrefix(String statsdPrefix) {
        this.statsdPrefix = statsdPrefix;
    }

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

    private static void applyConfigurationOptions(final VloReportGenerator vloReportGenerator, final Properties properties) {
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
                vloReportGenerator.setXmlOutputFile(xmlReportTarget);
            }
        }

        //statsd
        final String statsdHost = properties.getProperty("report.statsd.server.host");
        if (statsdHost != null) {
            final String statsdPort = properties.getProperty("report.statsd.server.port");
            final String statsdPrefix = properties.getProperty("report.statsd.prefix");
            if (statsdPort == null || statsdPrefix == null) {
                logger.error("One or more statsd properties have not been configured correctly");
                System.exit(1);
            }

            vloReportGenerator.setStatsdHost(statsdHost);
            vloReportGenerator.setStatsdPrefix(statsdPrefix);

            try {
                vloReportGenerator.setStatsdPort(Integer.parseInt(statsdPort));
            } catch (NumberFormatException ex) {
                logger.error("Invalid statsd port number: {}", statsdPort);
                logger.debug("Invalid statsd port number", ex);
                System.exit(1);
            }
        }
    }

}
