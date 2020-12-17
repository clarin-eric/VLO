package eu.clarin.cmdi.vlo.monitor.model;

import java.util.Optional;
import org.slf4j.event.Level;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class MonitorReportItem {

    private final Level level;
    private final Optional<String> field;
    private final String message;

    public MonitorReportItem(Level level, Optional<String> field, String message) {
        this.level = level;
        this.field = field;
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public Optional<String> getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s",
                level,
                field.map(f -> String.format("'%s'", f)).orElse("(No field)"),
                message);
    }

}
