package eu.clarin.vlo.sitemap.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "urlset", namespace="http://www.sitemaps.org/schemas/sitemap/0.9")
public class Sitemap {

	@XmlElement(name = "url")
	List<URL> urls = null;
	
	
	public List<URL> getUrls() {
		return urls;
	}


	public void setUrls(List<URL> urls) {
		this.urls = urls;
	}
	
	public void addURL(String url){
		if(urls == null)
			urls = new ArrayList<URL>();
		
		
	}



	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "url")
	public static class URL{
		
		private String loc = null;
				
		public URL(){}
		
		public URL(String s) {
			loc = s;
		}

		public String getLoc() {
			return loc;
		}

		public void setLoc(String loc) {
			this.loc = loc;
		}
		
		@Override
		public String toString() {
			return loc;
		}
		
		
	}
	
	
}
