package eu.clarin.cmdi.vlo.monitor.service;

import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
@Slf4j(topic = "VLO monitor report")
public class ReportingServiceImpl implements ReportingService {
    
    @Override
    public void report(Collection<MonitorReportItem> report) {
        if (report.isEmpty()) {
            log.info("No significant differences in comparison");
        } else {
            report.forEach(item -> {
                final String message = item.toString();
                switch (item.getLevel()) {
                    case WARN:
                        log.warn(message);
                        break;
                    case ERROR:
                        log.error(message);
                        break;
                    default:
                        log.info(message);
                        break;
                }
            });
        }
    }
}
