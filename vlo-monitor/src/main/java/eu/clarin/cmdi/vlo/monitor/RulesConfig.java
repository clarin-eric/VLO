package eu.clarin.cmdi.vlo.monitor;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * vlo.monitor.rules.fieldValuesDecreaseWarning.{field}=
 * vlo.monitor.rules.fieldValuesDecreaseError.{field}=
 * vlo.monitor.rules.totalRecordsDecreaseWarning=
 * vlo.monitor.rules.totalRecordsDecreaseError=
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Configuration
@ConfigurationProperties(prefix = "vlo.monitor.rules")
public class RulesConfig {

    private Map<String, String> fieldValuesDecreaseWarning;
    private Map<String, String> fieldValuesDecreaseError;
    private String totalRecordsDecreaseWarning;
    private String totalRecordsDecreaseError;

    public Map<String, String> getFieldValuesDecreaseWarning() {
        return fieldValuesDecreaseWarning;
    }

    public void setFieldValuesDecreaseWarning(Map<String, String> fieldValuesDecreaseWarning) {
        this.fieldValuesDecreaseWarning = fieldValuesDecreaseWarning;
    }

    public Map<String, String> getFieldValuesDecreaseError() {
        return fieldValuesDecreaseError;
    }

    public void setFieldValuesDecreaseError(Map<String, String> fieldValuesDecreaseError) {
        this.fieldValuesDecreaseError = fieldValuesDecreaseError;
    }

    public String getTotalRecordsDecreaseWarning() {
        return totalRecordsDecreaseWarning;
    }

    public void setTotalRecordsDecreaseWarning(String totalRecordsDecreaseWarning) {
        this.totalRecordsDecreaseWarning = totalRecordsDecreaseWarning;
    }

    public String getTotalRecordsDecreaseError() {
        return totalRecordsDecreaseError;
    }

    public void setTotalRecordsDecreaseError(String totalRecordsDecreaseError) {
        this.totalRecordsDecreaseError = totalRecordsDecreaseError;
    }

}
