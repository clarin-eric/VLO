package eu.clarin.cmdi.vlo.monitor;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    "vlo.monitor.rules.facetValuesDecreaseWarning.field1=25%",
    "vlo.monitor.rules.facetValuesDecreaseWarning.field2=100",
    "vlo.monitor.rules.facetValuesDecreaseError.field1=50%",
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

        assertNotNull(config.getFacetValuesDecreaseWarning(), "fieldValuesDecreaseWarning not null");
        final Map<String, String> facetValuesDecreaseWarning = config.getFacetValuesDecreaseWarning();
        assertEquals(2, facetValuesDecreaseWarning.size());
        assertEquals("25%", facetValuesDecreaseWarning.get("field1"), "fieldValuesDecreaseWarning for facet1");
        assertEquals("100", facetValuesDecreaseWarning.get("field2"), "fieldValuesDecreaseWarning for facet2");

        assertNotNull(config.getFacetValuesDecreaseError(), "fieldValuesDecreaseError not null");
        final Map<String, String> facetValuesDecreaseError = config.getFacetValuesDecreaseError();
        assertEquals(1, facetValuesDecreaseError.size());
        assertEquals("50%", facetValuesDecreaseError.get("field1"), "fieldValuesDecreaseError for facet1");
    }

    @Test
    public void testGetFieldRules() {
        Map<String, List<Rules.Rule>> fieldRules = rules.getFieldRules();
        assertNotNull(fieldRules);
        assertEquals(2, fieldRules.keySet().size());

        assertTrue(fieldRules.containsKey("field1"));
        final List<Rules.Rule> facet1rules = fieldRules.get("field1");
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

        assertTrue(fieldRules.containsKey("field2"));
        final List<Rules.Rule> facet2rules = fieldRules.get("field2");
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

    @Test
    public void testCreateRulesEmpty() {
        // Empty map
        final Stream<Rules.Rule> result = rules.createRules(Level.WARN, Optional.empty());
        assertEquals(0L, result.count());
    }

    @Test
    public void testCreateRulesAbsolute() {
        // Absolute thresholds
        List<Rules.Rule> result = rules.createRules(Level.WARN, Optional.of(ImmutableMap.<String, String>builder()
                .put("field1", "1")
                .put("field2", "2")
                .put("field3", "3")
                .build())).collect(Collectors.toList());
        assertEquals(3, result.size());
        assertThat(result, everyItem(isA(Rules.AbsoluteDecreaseRule.class)));
        assertThat(result, everyItem(hasProperty("level", equalTo(Level.WARN))));
        assertThat(result, allOf(
                hasItem(hasProperty("field", equalTo("field1"))),
                hasItem(hasProperty("field", equalTo("field2"))),
                hasItem(hasProperty("field", equalTo("field3")))));
        assertThat(result, allOf(
                hasItem(hasProperty("thresholdDiff", equalTo(1L))),
                hasItem(hasProperty("thresholdDiff", equalTo(2L))),
                hasItem(hasProperty("thresholdDiff", equalTo(3L)))));
    }

    @Test
    public void testCreateRulesRelative() {
        // Relative thresholds
        List<Rules.Rule> result = rules.createRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                .put("field1", "1%")
                .put("field2", "2%")
                .put("field3", " 3%")
                .build())).collect(Collectors.toList());
        assertEquals(3, result.size());
        assertThat(result, everyItem(isA(Rules.RatioDecreaseRule.class)));
        assertThat(result, everyItem(hasProperty("level", equalTo(Level.ERROR))));
        assertThat(result, allOf(
                hasItem(hasProperty("field", equalTo("field1"))),
                hasItem(hasProperty("field", equalTo("field2"))),
                hasItem(hasProperty("field", equalTo("field3")))));
        assertThat(result, allOf(
                hasItem(hasProperty("thresholdRatio", equalTo(.01))),
                hasItem(hasProperty("thresholdRatio", equalTo(.02))),
                hasItem(hasProperty("thresholdRatio", equalTo(.03)))));
    }

    @Test
    public void testCreateRulesErroneous() {
        try {
            rules.createRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                    .put("field1", "1")
                    .put("field1", "b")
                    .build()));
            fail("Syntax error should have caused error to be thrown");
        } catch (RuntimeException ex) {
            //need to get here before fail
        }
        try {
            rules.createRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                    .put("field1", "1")
                    .put("field1", "2a")
                    .build()));
            fail("Syntax error should have caused error to be thrown");
        } catch (RuntimeException ex) {
            //need to get here before fail
        }
        try {
            rules.createRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                    .put("field1", "1%")
                    .put("field1", "2%a")
                    .build()));
            fail("Syntax error should have caused error to be thrown");
        } catch (RuntimeException ex) {
            //need to get here before fail
        }
    }

    /**
     * Test of getAllFields method, of class RulesConfig.
     */
    @Test
    public void testGetAllFields() {
        final RulesConfig config = new RulesConfig();
        config.setFacetValuesDecreaseWarning(ImmutableMap.of("field1", "0", "field2", "0"));
        config.setFacetValuesDecreaseError(ImmutableMap.of("field1", "0", "field3", "0"));

        Rules instance = new Rules(config);

        Collection<String> result = instance.getAllFields();
        assertEquals(3, result.size());
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2"));
        assertTrue(result.contains("field3"));
    }

}
