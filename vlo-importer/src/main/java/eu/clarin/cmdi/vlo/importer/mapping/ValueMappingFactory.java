package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public interface ValueMappingFactory {
    public void createValueMapping(String fileName, FacetConceptMapping facetConceptMapping, FacetsMapping facetMapping) throws IOException, SAXException, ParserConfigurationException;

}