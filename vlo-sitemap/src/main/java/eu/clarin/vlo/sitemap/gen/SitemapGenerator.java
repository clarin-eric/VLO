package eu.clarin.vlo.sitemap.gen;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.vlo.sitemap.pojo.Sitemap;
import eu.clarin.vlo.sitemap.pojo.Sitemap.URL;
import eu.clarin.vlo.sitemap.pojo.SitemapIndex;
import eu.clarin.vlo.sitemap.services.SolrRecordUrlsService;
import eu.clarin.vlo.sitemap.services.SitemapIndexMarshaller;
import eu.clarin.vlo.sitemap.services.SitemapMarshaller;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jakarta.xml.bind.JAXBException;

public class SitemapGenerator {

    static Logger _logger = LoggerFactory.getLogger(SitemapGenerator.class);

    public void generateVLOSitemap() throws IOException, JAXBException {
        // clean sitemap output folder or create if it doesnt exist
        cleanOutputDir();

        // create urls
        final List<URL> urls
                = Streams.concat(Config.INCLUDE_URLS.stream().map(u -> new URL(u.trim())),
                        new SolrRecordUrlsService().getRecordURLS().stream()
                ).collect(Collectors.toUnmodifiableList());

        _logger.info("Total number of URLs " + urls.size());

        final List<String> sitemaps = createSitemaps(urls);
        createSitemapIndex(sitemaps);
    }

    private void cleanOutputDir() throws IOException {
        Path outputDir = Paths.get(Config.OUTPUT_FOLDER);
        if (!Files.exists(outputDir)) {// create folder if it doesnt exist
            Files.createDirectory(outputDir);
            _logger.info("Directory {} is created", outputDir);
        } else {// if exists deelte existing sitemap files
            Files.walkFileTree(outputDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.startsWith(Config.SITEMAP_INDEX_NAME)
                            || fileName.startsWith(Config.SITEMAP_NAME_PREFIX)) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            _logger.info("Directory {} is cleaned", outputDir);
        }
    }

    public void createSitemapIndex(List<String> maps) throws IOException, JAXBException {
        SitemapIndex index = new SitemapIndex();
        index.setMaps(maps.parallelStream()
                .map(map -> new SitemapIndex.Sitemap(map, (new SimpleDateFormat("yyyy-MM-dd")).format(new Date())))
                .collect(Collectors.toList()));

        new SitemapIndexMarshaller().marshall(index);
    }

    private List<String> createSitemaps(List<URL> urls) {
        final int maxUrlsPerSitemap = Integer.parseInt(Config.MAX_URLS_PER_SITEMAP);

        final ListeningExecutorService executor
                = MoreExecutors.listeningDecorator(
                        Executors.newFixedThreadPool(
                                Runtime.getRuntime().availableProcessors()));

        final ListeningExecutorService callbackExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

        try {
            final Stream<List<URL>> urlListsStream
                    = StreamSupport.stream(
                            Iterables.partition(urls, maxUrlsPerSitemap).spliterator(),
                            false);

            final AtomicInteger sitemapCnt = new AtomicInteger(1);
            final List<ListenableFuture<String>> futures = urlListsStream
                    .map(subURLs -> submitSubmapTask(subURLs, sitemapCnt, executor, callbackExecutor))
                    .collect(Collectors.toUnmodifiableList());

            return Futures.getUnchecked(Futures.allAsList(futures));
        } finally {
            try {
                executor.shutdown();
                if (!executor.isTerminated()) {
                    _logger.info("Waiting for sitemap generation to complete...");
                    if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                        _logger.warn("Timed out while waiting for executor to terminate");
                    }
                }
                callbackExecutor.shutdownNow();
            } catch (InterruptedException ex) {
                _logger.warn("Interrupted while waiting for executor to terminate");
            }
        }
    }

    private ListenableFuture<String> submitSubmapTask(List<URL> subURLs, AtomicInteger sitemapCnt, ListeningExecutorService executor, Executor callbackExecutor) {
        final String sitemapName = Config.SITEMAP_NAME_PREFIX + sitemapCnt.getAndIncrement();

        final ListenableFuture<String> submapFuture = executor.submit(() -> {
            Sitemap sitemap = new Sitemap();
            sitemap.setUrls(subURLs);

            final String sitemapFilename = Config.SITEMAP_BASE_URL + sitemapName + ".xml";
            new SitemapMarshaller().marshall(sitemap, sitemapName);
            return sitemapFilename;
        });

        Futures.addCallback(submapFuture, new SitemapTaskCallback(sitemapName, subURLs), callbackExecutor);

        return submapFuture;
    }

    private static class SitemapTaskCallback implements FutureCallback<String> {

        private final String sitemapName;
        private final List<URL> urls;

        public SitemapTaskCallback(String sitemapName, List<URL> urls) {
            this.sitemapName = sitemapName;
            this.urls = urls;
        }

        @Override
        public void onSuccess(String result) {
            _logger.debug("Completed async generation of {} ({} URLs)", sitemapName, urls.size());
        }

        @Override
        public void onFailure(Throwable t) {
            _logger.error("Failure while generating {} ({} URLs)", sitemapName, urls.size(), t);
        }
    }

}
