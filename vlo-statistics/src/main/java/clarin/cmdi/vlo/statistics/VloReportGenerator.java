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

import clarin.cmdi.vlo.statistics.collector.CollectionsCollector;
import clarin.cmdi.vlo.statistics.collector.FacetValueCountsCollector;
import clarin.cmdi.vlo.statistics.collector.RecordCountCollector;
import clarin.cmdi.vlo.statistics.collector.VloStatisticsCollector;
import clarin.cmdi.vlo.statistics.model.VloReport;
import clarin.cmdi.vlo.statistics.model.VloReport.CollectionCount;
import clarin.cmdi.vlo.statistics.model.VloReport.Facet;
import com.google.common.collect.ImmutableList;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
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
    private final List<VloStatisticsCollector> collectors;

    public VloReportGenerator(VloConfig config) {
        this.config = config;
        this.solrServer = new HttpSolrServer(config.getSolrUrl());

        //set the collectors that will be executed in order when creating the report
        this. collectors = ImmutableList.of(
                new RecordCountCollector(),
                new CollectionsCollector(),
                new FacetValueCountsCollector()
        //TODO: add collector for record ages
        );
    }

    public void run() throws SolrServerException, IOException, JAXBException {
        // Report object
        final VloReport report = new VloReport();

        try {
            // Gather statistics
            for (VloStatisticsCollector collector : collectors) {
                logger.info("Running {}", collector.getClass().getSimpleName());
                collector.collect(report, config, solrServer);
            }
        } finally {
            solrServer.shutdown();
        }

        if (xmlOutputFile != null) {
            // Write report
            marshallReport(report);
        }
        if (statsdHost != null) {
            sendToStatsd(report);
        }
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
        final StatsDClient client = new NonBlockingStatsDClient(statsdPrefix + ".index", statsdHost, statsdPort);
        client.gauge("nrRecords", report.getRecordCount());
        for (CollectionCount counts : report.getCollections()) {
            final String name = counts.getCollection().replaceAll("\\s", "_").replaceAll(":", "-");
            client.gauge("collections." + name, counts.getCount());
        }
        for (Facet facet : report.getFacets()) {
            client.gauge("facetValueCounts." + facet.getName(), facet.getValueCount());
        }
        client.stop();
    }

    public void setXmlOutputFile(File xmlOutputFile) {
        this.xmlOutputFile = xmlOutputFile;
    }

    public void setStatsdHost(String statsdHost) {
        this.statsdHost = statsdHost;
    }

    public void setStatsdPort(int port) {
        this.statsdPort = port;
    }

    public void setStatsdPrefix(String statsdPrefix) {
        this.statsdPrefix = statsdPrefix;
    }

}
