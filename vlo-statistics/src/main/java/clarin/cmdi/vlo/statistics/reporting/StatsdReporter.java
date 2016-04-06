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
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class StatsdReporter implements VloReportHandler {

    private final static Logger logger = LoggerFactory.getLogger(StatsdReporter.class);

    private final String statsdPrefix;
    private final String statsdHost;
    private final int statsdPort;

    public StatsdReporter(String statsdPrefix, String statsdHost, int statsdPort) {
        this.statsdPrefix = statsdPrefix;
        this.statsdHost = statsdHost;
        this.statsdPort = statsdPort;
    }

    @Override
    public void handleReport(VloReport report) {
        logger.info("Sending reports to statsd server {}:{} with prefix '{}'", statsdHost, statsdPort, statsdPrefix);
        final StatsDClient client = new NonBlockingStatsDClient(statsdPrefix + ".index", statsdHost, statsdPort);

        try {
            //send record count
            client.gauge("nrRecords", report.getRecordCount());

            //send collections
            report.getCollections().stream().forEach((counts) -> {
                final String name = counts.getCollection().replaceAll("\\s", "_").replaceAll(":", "-");
                client.gauge("collections." + name, counts.getCount());
            });

            //send facet values
            report.getFacets().stream().forEach((facet) -> {
                client.gauge("facetValueCounts." + facet.getName(), facet.getValueCount());
            });
        } finally {
            client.stop();
        }
    }

}
