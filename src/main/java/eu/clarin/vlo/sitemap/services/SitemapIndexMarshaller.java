package eu.clarin.vlo.sitemap.services;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.vlo.sitemap.gen.Config;
import eu.clarin.vlo.sitemap.pojo.SitemapIndex;

public class SitemapIndexMarshaller {
	
	static Logger _logger = LoggerFactory.getLogger(SitemapIndexMarshaller.class);
	
	public void marshall(SitemapIndex index) throws Exception{
		_logger.info("Generating index");
		
		String fileName = Config.OUTPUT_FOLDER + "/" + Config.SITEMAP_INDEX_NAME;
		
		JAXBContext jaxbContext = JAXBContext.newInstance(SitemapIndex.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(index, new File(fileName));
		
		_logger.info("Finished");
	}	

}
