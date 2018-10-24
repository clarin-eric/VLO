package eu.clarin.cmdi.vlo.importer.mapping;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.ximpleware.NavException;

import eu.clarin.cmdi.vlo.importer.Pattern;

public interface ConceptLinkPathMapper {
    
    Map<String, List<Pattern>> createConceptLinkPathMapping() throws NavException, URISyntaxException;
    
    public String getXsd();
    
    public Boolean useLocalXSDCache();

}
