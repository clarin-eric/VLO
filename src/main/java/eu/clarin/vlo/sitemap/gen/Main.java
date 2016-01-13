package eu.clarin.vlo.sitemap.gen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	static Logger _logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) throws Exception {

		SitemapGenerator gen = new SitemapGenerator();
		
		_logger.info("Started generating maps ... ");
		long startTime = System.currentTimeMillis();
		
		gen.generateVLOSitemap();
		
		long endTime = System.currentTimeMillis();
		_logger.info("Duration: " + (endTime - startTime) + "ms");
	}
	
}
