package eu.clarin.cmdi.vlo.monitor.service;

import eu.clarin.cmdi.vlo.monitor.Rules;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Collection;
import java.util.Collections;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
public class IndexStateCompareServiceImpl implements IndexStateCompareService {

    @Override
    public Collection<MonitorReportItem> compare(IndexState oldState, IndexState newState, Rules rules) {
        return Collections.emptyList();
    }
    
}
