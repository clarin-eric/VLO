package eu.clarin.cmdi.vlo.monitor.service;

import eu.clarin.cmdi.vlo.monitor.Rules;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Collection;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public interface IndexStateCompareService {
    
    Collection<MonitorReportItem> compare(IndexState oldState, IndexState newState, Rules rules);
}
