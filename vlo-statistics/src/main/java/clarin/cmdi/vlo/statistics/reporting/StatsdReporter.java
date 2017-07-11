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
package clarin.cmdi.vlo.statistics.reporting;

import clarin.cmdi.vlo.statistics.model.VloReport;
import clarin.cmdi.vlo.statistics.model.VloReportMarshaller;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientException;
import java.io.File;
import java.util.stream.Stream;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class StatsdReporter implements VloReportHandler {

    private static final String FACET_METRIC_PREFIX = "facetValueCounts.";
    private static final String COLLECTION_METRIC_PREFIX = "collections.";

    private final static Logger logger = LoggerFactory.getLogger(StatsdReporter.class);

    private final String statsdPrefix;
    private final String statsdHost;
    private final int statsdPort;
    private final File reportHistoryFile;
    private boolean noop;

    public StatsdReporter(String statsdPrefix, String statsdHost, int statsdPort, File reportHistoryFile) {
        this.statsdPrefix = statsdPrefix;
        this.statsdHost = statsdHost;
        this.statsdPort = statsdPort;
        this.reportHistoryFile = reportHistoryFile;
    }

    public void setNoop(boolean noop) {
        this.noop = noop;
    }

    @Override
    public void handleReport(VloReport report) {
        logger.info((noop ? "NOT sending" : "Sending") + " reports to statsd server {}:{} with prefix '{}'", statsdHost, statsdPort, statsdPrefix);
        final StatsDClient client = newClient();

        try {
            //get previous report so that we can process the removed values (it may be null)
            final VloReport previousReport = getPreviousReport();

            try {
                //send record count
                client.gauge("nrRecords", report.getRecordCount());

                //send collections
                report.getCollections().stream().forEach((collection) -> {
                    client.gauge(COLLECTION_METRIC_PREFIX + normaliseMetricName(collection.getCollection()), collection.getCount());
                });

                //send facet values
                report.getFacets().stream().forEach((facet) -> {
                    client.gauge(FACET_METRIC_PREFIX + normaliseMetricName(facet.getName()), facet.getValueCount());
                });

                if (previousReport != null) {
                    resetRemovedCollections(previousReport, report, client);
                    resetRemovedFacets(previousReport, report, client);
                }

            } finally {
                try {
                    client.stop();
                } finally {
                    //store the current report for the next time
                    updateReportHistory(report);
                }
            }
        } catch (JAXBException ex) {
            throw new RuntimeException("Could not (un)marshall report history for statsd", ex);
        }
    }

    private void resetRemovedCollections(final VloReport previousReport, VloReport report, final StatsDClient client) {
        final Stream<VloReport.CollectionCount> removedCollections
                = previousReport.getCollections().stream().filter((fromPrevious) -> {
                    //only keep collections that did not exist last time
                    return report.getCollections().stream().noneMatch((fromCurrent) -> {
                        return fromCurrent.getCollection().equals(fromPrevious.getCollection());
                    });
                });
        removedCollections.forEach((collection) -> {
            client.gauge(COLLECTION_METRIC_PREFIX + normaliseMetricName(collection.getCollection()), 0);
        });
    }

    private void resetRemovedFacets(final VloReport previousReport, VloReport report, final StatsDClient client) {
        final Stream<VloReport.Facet> removedFacets
                = previousReport.getFacets().stream().filter((fromPrevious) -> {
                    //only keep collections that did not exist last time
                    return report.getFacets().stream().noneMatch((fromCurrent) -> {
                        return fromCurrent.getName().equals(fromPrevious.getName());
                    });
                });
        removedFacets.forEach((facet) -> {
            client.gauge(FACET_METRIC_PREFIX + normaliseMetricName(facet.getName()), 0);
        });
    }

    private VloReport getPreviousReport() throws JAXBException {
        final VloReportMarshaller marshaller = new VloReportMarshaller();
        final VloReport previousReport;
        if (reportHistoryFile.exists() && reportHistoryFile.canRead()) {
            previousReport = marshaller.unmarshall(new StreamSource(this.reportHistoryFile));
        } else {
            logger.warn("No report history found (normal on first run), existing values will not be reset");
            previousReport = null;
        }
        return previousReport;
    }

    private void updateReportHistory(VloReport report) throws JAXBException {
        final VloReportMarshaller marshaller = new VloReportMarshaller();
        //store currentreport as history file
        if (this.reportHistoryFile.exists() && !this.reportHistoryFile.delete()) {
            logger.error("Could delete old history file");
        }
        marshaller.marshall(report, new StreamResult(reportHistoryFile));
    }

    private StatsDClient newClient() throws StatsDClientException {
        final StatsDClient client;
        if (noop) {
            client = new NoOpStatsDClient();
        } else {
            client = new NonBlockingStatsDClient(statsdPrefix + ".index", statsdHost, statsdPort);
        }
        return client;
    }

    private String normaliseMetricName(String name) {
        return name.replaceAll("[^A-z0-9]", "_");
    }

}
