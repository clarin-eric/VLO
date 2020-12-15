package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
public class IndexServiceImpl implements IndexService {

    @Override
    public Map<String, Integer> getValueCounts(String facet) {
        return ImmutableMap.of("test", 100);
    }
    
}
