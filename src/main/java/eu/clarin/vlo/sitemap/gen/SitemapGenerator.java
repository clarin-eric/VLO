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

import eu.clarin.vlo.sitemap.services.SOLRService;
import eu.clarin.vlo.sitemap.services.SitemapIndexMarshaller;
import eu.clarin.vlo.sitemap.services.SitemapMarshaller;
import eu.clarin.vlo.sitemap.services.VLOLinksLoader;
import eu.clarin.vlo.sitempa.pojo.Sitemap;
import eu.clarin.vlo.sitempa.pojo.Sitemap.URL;
import eu.clarin.vlo.sitempa.pojo.SitemapIndex;

public class SitemapGenerator {
			
	public void generateVLOSitemap(){
		try{
			SOLRService service = new SOLRService();
			
			List<URL> urls = VLOLinksLoader.loadLinks();			
			urls.addAll(service.getRecordURLS());			
			
			System.out.println("size is " + urls.size());
			
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
			List<URL> subURLs = urls.subList(ind, Math.min(ind + Config.MAX_ENTRIES, urls.size()));
			ind += Config.MAX_ENTRIES;
			
			Sitemap sitemap = new Sitemap();
			sitemap.setUrls(subURLs);
			
			String sitemapName = Config.VLO_SITEMAP_CHUNK_NAME.replace("xx", String.valueOf(sitemapCnt++));
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
