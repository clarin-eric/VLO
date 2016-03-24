package eu.clarin.vlo.sitemap.gen;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.vlo.sitemap.pojo.Sitemap;
import eu.clarin.vlo.sitemap.pojo.Sitemap.URL;
import eu.clarin.vlo.sitemap.pojo.SitemapIndex;
import eu.clarin.vlo.sitemap.services.SOLRService;
import eu.clarin.vlo.sitemap.services.SitemapIndexMarshaller;
import eu.clarin.vlo.sitemap.services.SitemapMarshaller;

public class SitemapGenerator {

    static Logger _logger = LoggerFactory.getLogger(SitemapGenerator.class);

    public void generateVLOSitemap() {
	try {

	    // clean sitemap output folder or create if it doesnt exist
	    cleanOutputDir();

	    // create urls
	    List<URL> urls = new LinkedList<>();

	    for (String staticURL : Config.INCLUDE_URLS)
		urls.add(new URL(staticURL.trim()));
	    urls.addAll(new SOLRService().getRecordURLS());

	    _logger.info("Total number of URLs " + urls.size());

	    createSitemapIndex(createSitemaps(urls));

	} catch (Exception e) {
	    _logger.error("Error while generating sitemaps", e);
	    throw new RuntimeException(e);
	}

    }

    private void cleanOutputDir() throws Exception {
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
			    || fileName.startsWith(Config.SITEMAP_NAME_PREFIX))
			Files.delete(file);
		    return FileVisitResult.CONTINUE;
		}
	    });
	    _logger.info("Directory {} is cleaned", outputDir);
	}
    }

    public void createSitemapIndex(List<String> maps) throws Exception {

	SitemapIndex index = new SitemapIndex();
	index.setMaps(maps.parallelStream()
		.map(map -> new SitemapIndex.Sitemap(map, (new SimpleDateFormat("yyyy-MM-dd")).format(new Date())))
		.collect(Collectors.toList()));

	new SitemapIndexMarshaller().marshall(index);
    }

    private List<String> createSitemaps(List<URL> urls) throws Exception {

	List<String> sitemaps = new LinkedList<String>();
	List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();

	int ind = 0;
	int sitemapCnt = 1;

	while (true) {
	    int maxUrlsPerSitemap = Integer.valueOf(Config.MAX_URLS_PER_SITEMAP);

	    List<URL> subURLs = urls.subList(ind, Math.min(ind + maxUrlsPerSitemap, urls.size()));
	    ind += maxUrlsPerSitemap;

	    Sitemap sitemap = new Sitemap();
	    sitemap.setUrls(subURLs);

	    String sitemapName = Config.SITEMAP_NAME_PREFIX + sitemapCnt++;
	    sitemaps.add(Config.SITEMAP_BASE_URL + sitemapName + ".xml");

	    tasks.add(() -> new SitemapMarshaller().marshall(sitemap, sitemapName));

	    if (ind >= urls.size())
		break;
	}

	ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	List<Future<Void>> results = executor.invokeAll(tasks);

	for (Future res : results)
	    res.get();

	executor.shutdown();

	return sitemaps;
    }

}
