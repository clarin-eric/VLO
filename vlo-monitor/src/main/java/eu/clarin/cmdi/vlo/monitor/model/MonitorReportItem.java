package eu.clarin.cmdi.vlo.monitor.model;

import eu.clarin.cmdi.vlo.monitor.service.RulesService.Rule;
import java.util.Optional;
import org.slf4j.event.Level;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class MonitorReportItem {

    private final Optional<String> value;
    private final String message;
    private final Rule rule;

    public MonitorReportItem(Rule rule, Optional<String> value, String message) {
        this.rule = rule;
        this.value = value;
        this.message = message;
    }

    public Level getLevel() {
        return rule.getLevel();
    }

    public Optional<String> getField() {
        return Optional.ofNullable(rule.getField());
    }

    public Optional<String> getValue() {
        return value;
    }

    public Rule getRule() {
        return rule;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s - %s",
                rule.getScope().toString(),
                getLevel(),
                getField().map(f -> String.format("%s:'%s'", f, value.orElse("-"))).orElse("(No field)"),
                message);
    }

}
