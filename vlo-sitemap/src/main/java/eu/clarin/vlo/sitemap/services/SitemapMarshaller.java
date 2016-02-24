package eu.clarin.vlo.sitemap.services;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.vlo.sitemap.gen.Config;
import eu.clarin.vlo.sitemap.pojo.Sitemap;

public class SitemapMarshaller {
	
	static Logger _logger = LoggerFactory.getLogger(SitemapMarshaller.class);
	
	public Void marshall(Sitemap sitemap, String fileName) throws Exception{
		fileName = Config.OUTPUT_FOLDER + "/" + fileName + ".xml";
		_logger.info("Generating " + fileName);
		File xml = new File(fileName);
		JAXBContext jaxbContext = JAXBContext.newInstance(Sitemap.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(sitemap, xml);		
		
		_logger.info("Finished with " + fileName + String.format("total size: %.2f", (1.0 * xml.length()/(1024 * 1024))) + "MB");		
		return null;
	}
	

}
