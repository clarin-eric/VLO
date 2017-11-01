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
import clarin.cmdi.vlo.statistics.reporting.VloReportHandler;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class VloReportGenerator {

    private final static Logger logger = LoggerFactory.getLogger(VloReportGenerator.class);

    private final VloConfig config;
    private final HttpSolrClient solrClient;
    private final List<VloStatisticsCollector> collectors;
    private final List<VloReportHandler> resultHandlers;

    public VloReportGenerator(VloConfig config) {
        this(config,
                //set the default collectors that will be executed in order when creating the report
                ImmutableList.of(
                        new RecordCountCollector(),
                        new CollectionsCollector(),
                        new FacetValueCountsCollector()
                //TODO: add collector for data providers
                //TODO: add collector for record ages
                ));
    }

    public VloReportGenerator(VloConfig config, List<VloStatisticsCollector> collectors) {
        this(config, collectors, new ArrayList<>());
    }

    public VloReportGenerator(VloConfig config, List<VloStatisticsCollector> collectors, List<VloReportHandler> resultHandlers) {
        this.config = config;
        this.collectors = collectors;
        this.resultHandlers = resultHandlers;
        this.solrClient = new HttpSolrClient.Builder(config.getSolrUrl()).build();
    }

    public void run() throws SolrServerException, IOException, JAXBException {
        // Empty report object
        final VloReport report = new VloReport();

        // Gather statistics
        try {
            for (VloStatisticsCollector collector : collectors) {
                logger.info("Running {}", collector.getClass().getSimpleName());
                collector.collect(report, config, solrClient);
            }
        } finally {
            solrClient.close();
        }

        // Handle results (write output etc)
        for (VloReportHandler handler : resultHandlers) {
            logger.info("Handling results with {}", handler.getClass().getSimpleName());
            handler.handleReport(report);
        }
    }

    public List<VloReportHandler> getResultHandlers() {
        return resultHandlers;
    }

}
