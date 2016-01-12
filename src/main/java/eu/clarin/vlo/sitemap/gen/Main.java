package eu.clarin.vlo.sitemap.gen;

public class Main {
	
	public static void main(String[] args) throws Exception {

		SitemapGenerator gen = new SitemapGenerator();
		
		System.out.println("Started generating maps ... ");
		long startTime = System.currentTimeMillis();
		
		gen.generateVLOSitemap();
		
		long endTime = System.currentTimeMillis();
		System.out.println("Duration: " + (endTime - startTime) + "ms");
	}
	
}
