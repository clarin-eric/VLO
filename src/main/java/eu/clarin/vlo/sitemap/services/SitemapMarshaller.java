package eu.clarin.vlo.sitemap.services;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import eu.clarin.vlo.sitemap.pojo.Sitemap;

public class SitemapMarshaller {
	
	
	public Void marshall(Sitemap sitemap, String fileName) throws Exception{
		System.out.println("Generating " + fileName);
		File xml = new File(fileName);
		JAXBContext jaxbContext = JAXBContext.newInstance(Sitemap.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(sitemap, xml);
		
		
		
		System.out.println("Finished with " + fileName + String.format("total size: %.2f", (1.0 * xml.length()/(1024 * 1024))) + "MB");		
		return null;
	}
	

}
