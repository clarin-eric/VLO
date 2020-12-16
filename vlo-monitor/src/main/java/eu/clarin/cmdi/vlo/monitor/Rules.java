package eu.clarin.cmdi.vlo.monitor;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Component
public class Rules {

    private final RulesConfig config;

    public Rules(RulesConfig rulesConfig) {
        this.config = rulesConfig;
    }

    public Collection<String> getAllFields() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        if (config.getFacetValuesDecreaseWarning() != null) {
            builder.addAll(config.getFacetValuesDecreaseWarning().keySet());
        }
        if (config.getFacetValuesDecreaseError() != null) {
            builder.addAll(config.getFacetValuesDecreaseError().keySet());
        }
        return builder.build();
    }

    public RulesConfig getConfig() {
        return config;
    }
}
