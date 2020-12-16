package eu.clarin.cmdi.vlo.monitor;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
    "vlo.monitor.rules.facetValuesDecreaseWarning.facet1=25%",
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

    @Test
    public void testGetFieldRules() {
        Map<String, List<Rules.Rule>> fieldRules = rules.getFieldRules();
        assertNotNull(fieldRules);
        assertEquals(2, fieldRules.keySet().size());

        assertTrue(fieldRules.containsKey("facet1"));
        final List<Rules.Rule> facet1rules = fieldRules.get("facet1");
        assertEquals(2, facet1rules.size());
        assertThat(facet1rules,
                allOf(
                        hasItem(
                                allOf(
                                        isA(Rules.RatioDecreaseRule.class),
                                        hasProperty("level", equalTo(Level.WARN)),
                                        hasProperty("thresholdRatio", equalTo(.25))
                                )),
                        hasItem(
                                allOf(
                                        isA(Rules.RatioDecreaseRule.class),
                                        hasProperty("level", equalTo(Level.ERROR)),
                                        hasProperty("thresholdRatio", equalTo(.50))
                                )))
        );

        assertTrue(fieldRules.containsKey("facet2"));
        final List<Rules.Rule> facet2rules = fieldRules.get("facet2");
        assertEquals(1, facet2rules.size());
        assertThat(facet2rules,
                hasItem(
                        allOf(
                                isA(Rules.AbsoluteDecreaseRule.class),
                                hasProperty("level", equalTo(Level.WARN)),
                                hasProperty("thresholdDiff", equalTo(Long.valueOf(100)))
                        ))
        );

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
