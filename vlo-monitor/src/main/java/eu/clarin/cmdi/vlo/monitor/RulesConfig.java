package eu.clarin.cmdi.vlo.monitor;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 
 * vlo.monitor.rules.facetValuesDecreaseWarning.[facet]=
 * vlo.monitor.rules.facetValuesDecreaseError.[facet]=
 * vlo.monitor.rules.totalRecordsDecreaseWarning=
 * vlo.monitor.rules.totalRecordsDecreaseError=
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Configuration
@ConfigurationProperties(prefix = "vlo.monitor.rules")
public class RulesConfig {

    private Map<String, String> facetValuesDecreaseWarning;
    private Map<String, String> facetValuesDecreaseError;
    private String totalRecordsDecreaseWarning;
    private String totalRecordsDecreaseError;
    
    public Map<String, String> getFacetValuesDecreaseWarning() {
        return facetValuesDecreaseWarning;
    }

    public void setFacetValuesDecreaseWarning(Map<String, String> facetValuesDecreaseWarning) {
        this.facetValuesDecreaseWarning = facetValuesDecreaseWarning;
    }

    public Map<String, String> getFacetValuesDecreaseError() {
        return facetValuesDecreaseError;
    }

    public void setFacetValuesDecreaseError(Map<String, String> facetValuesDecreaseError) {
        this.facetValuesDecreaseError = facetValuesDecreaseError;
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
