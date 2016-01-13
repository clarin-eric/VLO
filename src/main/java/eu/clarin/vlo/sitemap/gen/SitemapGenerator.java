package eu.clarin.vlo.sitemap.gen;

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
			
	public void generateVLOSitemap(){
		try{
			List<URL> urls = new LinkedList<>();
			
			for(String staticURL: Config.INCLUDE_URLS.split(","))
				urls.add(new URL(staticURL.trim()));					
			urls.addAll(new SOLRService().getRecordURLS());			
			
			_logger.info("Total number of URLs " + urls.size());
			
			createSitemapIndex(createSitemaps(urls));
			
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		
	}
	
	public void createSitemapIndex(List<String> maps) throws Exception{
		 
		 SitemapIndex index = new SitemapIndex();
		 index.setMaps( 
				 maps
				.parallelStream()
				.map(map -> new SitemapIndex.Sitemap(map, (new SimpleDateFormat("yyyy-MM-dd")).format(new Date())))
				.collect(Collectors.toList())
		);
		
		new SitemapIndexMarshaller().marshall(index);
	}
		
	
	private List<String> createSitemaps(List<URL> urls) throws Exception{
		
		List<String> sitemaps = new LinkedList<String>();		
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		
		int ind = 0;
		int sitemapCnt = 1;
		
		while(true){
			int maxUrlsPerSitemap = Integer.valueOf(Config.MAX_URLS_PER_SITEMAP);
			
			List<URL> subURLs = urls.subList(ind, Math.min(ind + maxUrlsPerSitemap, urls.size()));
			ind += maxUrlsPerSitemap;
			
			Sitemap sitemap = new Sitemap();
			sitemap.setUrls(subURLs);
			
			String sitemapName = Config.SITEMAP_NAME_PREFIX + sitemapCnt++;
			sitemaps.add(sitemapName);			
			
			
			tasks.add(() -> new SitemapMarshaller().marshall(sitemap, sitemapName));
			
			if(ind >= urls.size())
				break;
		}
				
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		 List<Future<Void>>  results = executor.invokeAll(tasks);
		 
		 for(Future res: results) res.get();
		 
		executor.shutdown();
		
		return sitemaps;
	}
	
	
	
	

}
