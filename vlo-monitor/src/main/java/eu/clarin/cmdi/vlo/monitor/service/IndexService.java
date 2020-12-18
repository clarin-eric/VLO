package eu.clarin.cmdi.vlo.monitor.service;

import java.util.Map;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public interface IndexService {
    
    Map<String, Long> getValueCounts(String field);

    Long getTotalRecordCount();
    
}
