package eu.clarin.vlo.sitemap.pojo;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sitemapindex", namespace="http://www.sitemaps.org/schemas/sitemap/0.9")
public class SitemapIndex {
	
	
	@XmlElement(name = "sitemap")
	List<Sitemap> maps = null;
	
		
	
	public List<Sitemap> getMaps() {
		return maps;
	}
	
	public void setMaps(List<Sitemap> maps) {
		this.maps = maps;
	}
	

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "sitemap")
	public static class Sitemap{
		
		private String loc = null;
		private String lastmod = null;
		
		public Sitemap(){}
		
		public Sitemap(String loc, String lastmod){
			this.loc = loc;
			this.lastmod = lastmod;
		}
		
		public String getLoc() {
			return loc;
		}
		public void setLoc(String loc) {
			this.loc = loc;
		}
		public String getLastmod() {
			return lastmod;
		}
		public void setLastmod(String lastmod) {
			this.lastmod = lastmod;
		}
						
	}

}
