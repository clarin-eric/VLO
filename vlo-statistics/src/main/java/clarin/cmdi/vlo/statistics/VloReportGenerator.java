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
import com.oracle.xmlns.internal.webservices.jaxws_databinding.ObjectFactory;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
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
    private final File outputLocation;
    private final HttpSolrServer solrServer;

    public VloReportGenerator(VloConfig config, File outputLocation) {
        this.config = config;
        this.outputLocation = outputLocation;
        this.solrServer = new HttpSolrServer(config.getSolrUrl());
    }

    public void run() throws SolrServerException, IOException, JAXBException {
        // Report object
        final VloReport report = new VloReport();

        // Gather statistics
        report.setRecordCount(getRecordCount());
        report.setCollections(obtainCollectionCounts());

        // Write report
        marshallReport(report);
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
        logger.info("Collection field: {}", collectionField.getValues());

        final List<CollectionCount> counts
                = collectionField.getValues().stream().map((count) -> {
                    CollectionCount collectionCount = new CollectionCount();
                    collectionCount.setCollection(count.getName());
                    collectionCount.setCount(count.getCount());
                    return collectionCount;
                }).collect(Collectors.toList());
        return counts;
    }

    public static void main(String[] args) throws MalformedURLException, IOException, SolrServerException, JAXBException {
        if (args.length < 2) {
            logger.error("Provide configuration location and output file as parameters");
            System.exit(1);
        }

        final File configLocation = new File(args[0]);
        if (!configLocation.exists()) {
            logger.error("Configuration file {} does not exist", configLocation);
            System.exit(1);
        }

        final File outputLocation = new File(args[1]);
        if (outputLocation.exists() && (outputLocation.isDirectory() || !outputLocation.canWrite())) {
            logger.error("Cannot write to output file {}", outputLocation);
            System.exit(1);
        }

        // load config
        logger.info("Loading configuration from {}", configLocation);
        final XmlVloConfigFactory xmlVloConfigFactory
                = new XmlVloConfigFactory(configLocation.toURI().toURL());
        final VloConfig vloConfig = xmlVloConfigFactory.newConfig();

        // start report generator
        logger.info("Gathering statistics...");
        final VloReportGenerator vloReportGenerator = new VloReportGenerator(vloConfig, outputLocation);
        vloReportGenerator.run();
    }

    private void marshallReport(VloReport report) throws JAXBException {
        // Prepare marshaller
        final JAXBContext jc = JAXBContext.newInstance(VloReport.class);
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // Write to target
        logger.info("Writing report to {}", outputLocation);
        marshaller.marshal(report, outputLocation);

        // Write to stdout
        marshaller.marshal(report, System.out);
    }

}
