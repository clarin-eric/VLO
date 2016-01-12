package eu.clarin.vlo.sitemap.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import eu.clarin.vlo.sitemap.pojo.Sitemap.URL;

public class VLOLinksLoader {
	
	public static List<URL> loadLinks() throws Exception{
		
		List<URL> urls = new LinkedList<URL>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(VLOLinksLoader.class.getClassLoader().getResource("vlo-links.txt").getFile()))){
		    String line;
		    while ((line = br.readLine()) != null) {
		    	urls.add(new URL(line));
		    }
		}
		
		return urls;
		
	}

}
