package eu.clarin.vlo.sitemap.gen;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    private static final String PROPERTIES_FILE = "config.properties";

    static final Logger _logger = LoggerFactory.getLogger(Config.class);

    public static final String MAX_URLS_PER_SITEMAP;
    public static final String OUTPUT_FOLDER;
    public static final String SITEMAP_NAME_PREFIX;
    public static final String SITEMAP_INDEX_NAME;
    public static final String RECORD_URL_TEMPLATE;
    public static final String SOLR_QUERY_URL;
    public static final String INCLUDE_URLS;

    static {
	Properties prop = new Properties();
	_logger.info("reading properties from {}", PROPERTIES_FILE);	
	try {
	    prop.load(new FileInputStream(PROPERTIES_FILE));	    

	} catch (Exception e) {
	    _logger.warn("configuration file {} has not been found. Will use the one in classpath", PROPERTIES_FILE);
	   
	    try {
		prop.load(Config.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
	    } catch (IOException e1) {
		throw new RuntimeException("Unable to read properties file. Can not continue without it!", e1);
	    }
	    
	}	

	MAX_URLS_PER_SITEMAP = prop.getProperty("MAX_URLS_PER_SITEMAP");
	OUTPUT_FOLDER = prop.getProperty("OUTPUT_FOLDER");
	SITEMAP_NAME_PREFIX = prop.getProperty("SITEMAP_NAME_PREFIX");
	SITEMAP_INDEX_NAME = prop.getProperty("SITEMAP_INDEX_NAME");
	RECORD_URL_TEMPLATE = prop.getProperty("RECORD_URL_TEMPLATE");
	SOLR_QUERY_URL = prop.getProperty("SOLR_QUERY_URL");
	INCLUDE_URLS = prop.getProperty("INCLUDE_URLS");
	_logger.info("properties were successfully read");

    }

}
