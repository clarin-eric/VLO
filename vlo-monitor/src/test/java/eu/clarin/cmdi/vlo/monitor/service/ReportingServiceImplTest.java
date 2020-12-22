package eu.clarin.cmdi.vlo.monitor.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import eu.clarin.cmdi.vlo.monitor.service.RulesService.Rule;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class ReportingServiceImplTest {

    /**
     * Test of report method, of class ReportingServiceImpl.
     */
    @Test
    public void testReport() {
        final ReportingServiceImpl instance = new ReportingServiceImpl();
        // set up log interception
        final Logger logger = (Logger) LoggerFactory.getLogger("VLO monitor report");
        final ListAppender<ILoggingEvent> logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        final Rule rule1 = new RulesService.AbsoluteDecreaseRule(RulesService.RuleScope.FIELD_VALUE_COUNT, "field1", Level.WARN, 10L);
        final Rule rule2 = new RulesService.RatioDecreaseRule(RulesService.RuleScope.TOTAL_RECORD_COUNT, null, Level.ERROR, .5);
        final Collection<MonitorReportItem> report = ImmutableList.of(
                new MonitorReportItem(rule1, Optional.of("value1"), String.format("Triggered by rule: [%s]", rule1.toString())),
                new MonitorReportItem(rule1, Optional.of("value2"), String.format("Triggered by rule: [%s]", rule1.toString())),
                new MonitorReportItem(rule1, Optional.empty(), String.format("Triggered by rule: [%s]", rule1.toString())),
                new MonitorReportItem(rule2, Optional.of("value3"), String.format("Triggered by rule: [%s]", rule2.toString())),
                new MonitorReportItem(rule2, Optional.of("value4"), String.format("Triggered by rule: [%s]", rule2.toString())),
                new MonitorReportItem(rule2, Optional.empty(), String.format("Triggered by rule: [%s]", rule2.toString()))
        );
        instance.report(report);

        assertThat(logAppender.list, hasSize(6));
        assertThat(logAppender.list,
                hasItems(
                        hasProperty("level", equalTo(ch.qos.logback.classic.Level.WARN)),
                        hasProperty("level", equalTo(ch.qos.logback.classic.Level.ERROR))
                )
        );
    }

}
