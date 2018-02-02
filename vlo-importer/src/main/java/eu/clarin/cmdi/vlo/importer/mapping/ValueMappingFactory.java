package eu.clarin.cmdi.vlo.importer.mapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class ValueMappingFactory {
    private final static Logger LOG = LoggerFactory.getLogger(ValueMappingFactory.class);
    
    public static final Map<String, List<ConditionTargetSet>> getValueMappings(String fileName, FacetConceptMapping conceptMapping){
        HashMap<String, List<ConditionTargetSet>> valueMappings = new HashMap<String, List<ConditionTargetSet>>();
        
        SAXParserFactory fac = SAXParserFactory.newInstance();
        fac.setXIncludeAware(true);
        fac.setNamespaceAware(true);
        
        

            try {
                fac.newSAXParser().parse(fileName, new ValueMappingsHandler(conceptMapping, valueMappings));
            } 
            catch (SAXException | IOException | ParserConfigurationException ex) {
                LOG.error("Value Mappings not initialized!", ex);
            }
            
            return valueMappings;
    }

}
