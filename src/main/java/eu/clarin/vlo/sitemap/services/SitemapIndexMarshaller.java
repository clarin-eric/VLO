package eu.clarin.vlo.sitemap.services;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import eu.clarin.vlo.sitemap.gen.Config;
import eu.clarin.vlo.sitemap.pojo.SitemapIndex;

public class SitemapIndexMarshaller {
	
	
	public void marshall(SitemapIndex index) throws Exception{
		System.out.println("Generating index");
		JAXBContext jaxbContext = JAXBContext.newInstance(SitemapIndex.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(index, new File(Config.VLO_SITEMAP_INDEX_NAME));
		
		System.out.println("Finished");
	}	

}
