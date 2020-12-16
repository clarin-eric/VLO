package eu.clarin.cmdi.vlo.monitor;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties
@ContextConfiguration(classes = {RulesConfig.class, Rules.class})
@TestPropertySource(properties = {
            "vlo.monitor.rules.facetValuesDecreaseWarning[facet1]=25%",
            "vlo.monitor.rules.facetValuesDecreaseWarning.facet2=100",
            "vlo.monitor.rules.facetValuesDecreaseError.facet1=50%",
            "vlo.monitor.rules.totalRecordsDecreaseWarning=10%",
            "vlo.monitor.rules.totalRecordsDecreaseError=25%"})
public class RulesTest {

    @Autowired
    private Rules rules;

    public RulesTest() {
    }

    @Test
    public void testReadConfig() {
        final RulesConfig config = rules.getConfig();
        assertNotNull(config);

        assertNotNull(config.getTotalRecordsDecreaseWarning(), "totalRecordsDecreaseWarning not null");
        assertEquals("10%", config.getTotalRecordsDecreaseWarning(), "totalRecordsDecreaseWarning value");

        assertNotNull(config.getFacetValuesDecreaseWarning(), "facetValuesDecreaseWarning not null");
        final Map<String, String> facetValuesDecreaseWarning = config.getFacetValuesDecreaseWarning();
        assertEquals(2, facetValuesDecreaseWarning.size());
        assertEquals("25%", facetValuesDecreaseWarning.get("facet1"), "facetValuesDecreaseWarning for facet1");
        assertEquals("100", facetValuesDecreaseWarning.get("facet2"), "facetValuesDecreaseWarning for facet2");

        assertNotNull(config.getFacetValuesDecreaseError(), "facetValuesDecreaseError not null");
        final Map<String, String> facetValuesDecreaseError = config.getFacetValuesDecreaseError();
        assertEquals(1, facetValuesDecreaseError.size());
        assertEquals("50%", facetValuesDecreaseError.get("facet1"), "facetValuesDecreaseError for facet1");
    }

    /**
     * Test of getAllFields method, of class RulesConfig.
     */
    @Test
    public void testGetAllFields() {
        final RulesConfig config = new RulesConfig();
        config.setFacetValuesDecreaseWarning(ImmutableMap.of("facet1", "0", "facet2", "0"));
        config.setFacetValuesDecreaseError(ImmutableMap.of("facet1", "0", "facet3", "0"));

        Rules instance = new Rules(config);

        Collection<String> result = instance.getAllFields();
        assertEquals(3, result.size());
        assertTrue(result.contains("facet1"));
        assertTrue(result.contains("facet2"));
        assertTrue(result.contains("facet3"));
    }

}
