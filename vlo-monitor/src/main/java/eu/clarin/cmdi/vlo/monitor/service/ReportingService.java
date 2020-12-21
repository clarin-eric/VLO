package eu.clarin.cmdi.vlo.monitor.service;

import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Collection;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public interface ReportingService {

    void report(Collection<MonitorReportItem> report);
}
