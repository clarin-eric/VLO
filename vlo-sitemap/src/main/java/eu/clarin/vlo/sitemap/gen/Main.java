package eu.clarin.vlo.sitemap.gen;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final String PROPERTIES_FILE = "config.properties";

    static Logger _logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

	String proprtiesFile = args.length > 0 ? args[0] : PROPERTIES_FILE;

	Configuration prop;
	//Properties prop = new Properties();
	try {
	    _logger.info("reading properties from {}", proprtiesFile);
	    prop = new PropertiesConfiguration(proprtiesFile);
	    //prop.load(new FileInputStream(proprtiesFile));
	} catch (Exception e) {
	    throw new RuntimeException("Unable to read configuration file: " + proprtiesFile, e);
	}

	Config.MAX_URLS_PER_SITEMAP = prop.getString("MAX_URLS_PER_SITEMAP");
	Config.OUTPUT_FOLDER = prop.getString("OUTPUT_FOLDER");
	Config.VLO_URL = prop.getString("VLO_URL");
	Config.SITEMAP_BASE_URL = prop.getString("SITEMAP_BASE_URL");
	Config.SITEMAP_NAME_PREFIX = prop.getString("SITEMAP_NAME_PREFIX");
	Config.SITEMAP_INDEX_NAME = prop.getString("SITEMAP_INDEX_NAME");
	Config.RECORD_URL_TEMPLATE = prop.getString("RECORD_URL_TEMPLATE");
        Config.SOLR_USER = prop.getString("SOLR_USER");
        Config.SOLR_PASS = prop.getString("SOLR_PASS");
	Config.SOLR_QUERY_URL = prop.getString("SOLR_QUERY_URL");
	Config.INCLUDE_URLS = prop.getList("INCLUDE_URLS");
	
	_logger.info("properties were successfully read");

	SitemapGenerator gen = new SitemapGenerator();

	_logger.info("Started generating maps ... ");
	long startTime = System.currentTimeMillis();

	gen.generateVLOSitemap();

	long endTime = System.currentTimeMillis();
	_logger.info("Duration: " + (endTime - startTime) + "ms");
    }

}
