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

import com.google.common.math.Stats;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.lf5.util.StreamUtils;

/**
 *
 * @author twagoo
 */
public class MetadataImporterBenchmarkRunner extends MetadataImporterRunner {

    private final static String CONF_FILE = "/Users/twagoo/vlo/vlo3/VloConfig.xml";
    private final static String BASE_OUT_FILE = "/Users/twagoo/vlo/benchmark/log-";
    protected static final int RUNS = 5;

    public static void main(String[] args) throws MalformedURLException, IOException {
        final File outFile = new File(BASE_OUT_FILE + System.currentTimeMillis() + ".txt");

        final XmlVloConfigFactory configFactory = new XmlVloConfigFactory(new File(CONF_FILE).toURI().toURL());
        final VloConfig config = configFactory.newConfig();

        final List<Long> times = new ArrayList<>(RUNS);

        try (PrintStream out = new PrintStream(outFile)) {
            out.printf("File processing threads: %d\n", config.getFileProcessingThreads());
            out.printf("Solr threads: %d\n", config.getSolrThreads());
            out.printf("Min docs in Solr queue: %d\n", config.getMinDocsInSolrQueue());
            out.printf("Max docs in list: %d\n", config.getMaxDocsInList());

            for (int i = 0; i < RUNS; i++) {
                final MetadataImporter importer = MetadataImporterRunner.runImporter(config, null);
                out.printf("Run %d: %d ms (%d documents)\n", i, importer.getTime(), importer.nrOFDocumentsSent.get());

                //keep for stats
                times.add(importer.getTime());
            }

            final Stats stats = Stats.of(times);
            out.printf("Total time: %s\n", stats.sum());
            out.printf("Average time: %s ms (%s s)\n", stats.mean(), stats.mean() / 1000);
            out.printf("Stdev: %s ms (%s s)\n", stats.sampleStandardDeviation(), stats.sampleStandardDeviation() / 1000);
        }

        System.out.println("------------------\nEND OF BENCHMARK\n------------------");
        System.out.println(outFile.getAbsolutePath());
        try (FileInputStream fileInputStream = new FileInputStream(outFile)) {
            StreamUtils.copy(fileInputStream, System.out);
        }
    }
}
