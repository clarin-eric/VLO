package eu.clarin.cmdi.vlo.monitor;

import java.util.Map;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public interface IndexService {
    
    Map<String, Integer> getValueCounts(String facet);
    
}
