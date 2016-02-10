package eu.clarin.vlo.sitemap.gen;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final String PROPERTIES_FILE = "config.properties";

    static Logger _logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

	String proprtiesFile = args.length > 0 ? args[0] : PROPERTIES_FILE;

	Properties prop = new Properties();
	try {
	    _logger.info("reading properties from {}", proprtiesFile);
	    prop.load(new FileInputStream(proprtiesFile));
	} catch (Exception e) {
	    throw new RuntimeException("Config file \"config.propertis\" is missing", e);
	}

	Config.MAX_URLS_PER_SITEMAP = prop.getProperty("MAX_URLS_PER_SITEMAP");
	Config.OUTPUT_FOLDER = prop.getProperty("OUTPUT_FOLDER");
	Config.SITEMAP_NAME_PREFIX = prop.getProperty("SITEMAP_NAME_PREFIX");
	Config.SITEMAP_INDEX_NAME = prop.getProperty("SITEMAP_INDEX_NAME");
	Config.RECORD_URL_TEMPLATE = prop.getProperty("RECORD_URL_TEMPLATE");
	Config.SOLR_QUERY_URL = prop.getProperty("SOLR_QUERY_URL");
	Config.INCLUDE_URLS = prop.getProperty("INCLUDE_URLS");
	
	_logger.info("properties were successfully read");

	SitemapGenerator gen = new SitemapGenerator();

	_logger.info("Started generating maps ... ");
	long startTime = System.currentTimeMillis();

	gen.generateVLOSitemap();

	long endTime = System.currentTimeMillis();
	_logger.info("Duration: " + (endTime - startTime) + "ms");
    }

}
