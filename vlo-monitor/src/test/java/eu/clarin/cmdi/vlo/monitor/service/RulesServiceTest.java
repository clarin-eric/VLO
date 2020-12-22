package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.monitor.RulesConfig;
import eu.clarin.cmdi.vlo.monitor.model.Rule;
import eu.clarin.cmdi.vlo.monitor.model.Rule.AbsoluteDecreaseRule;
import eu.clarin.cmdi.vlo.monitor.model.Rule.RatioDecreaseRule;
import eu.clarin.cmdi.vlo.monitor.model.Rule.RuleScope;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
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
@ContextConfiguration(classes = {RulesConfig.class, RulesService.class})
@TestPropertySource(properties = {
    "vlo.monitor.rules.fieldValuesDecreaseWarning.field1=25%",
    "vlo.monitor.rules.fieldValuesDecreaseWarning.field2=100",
    "vlo.monitor.rules.fieldValuesDecreaseError.field1=50%",
    "vlo.monitor.rules.totalRecordsDecreaseWarning=1000",
    "vlo.monitor.rules.totalRecordsDecreaseError=25%"})
public class RulesServiceTest {

    @Autowired
    private RulesService rules;

    public RulesServiceTest() {
    }

    @Test
    public void testReadConfig() {
        //Note: rule configuration is loaded from properties (see @TestPropertySource annotation)
        final RulesConfig config = rules.getConfig();
        assertNotNull(config);

        assertNotNull(config.getTotalRecordsDecreaseWarning(), "totalRecordsDecreaseWarning not null");
        assertEquals("1000", config.getTotalRecordsDecreaseWarning(), "totalRecordsDecreaseWarning value");
        
        assertNotNull(config.getTotalRecordsDecreaseError(), "totalRecordsDecreaseError not null");
        assertEquals("25%", config.getTotalRecordsDecreaseError(), "totalRecordsDecreaseError value");

        assertNotNull(config.getFieldValuesDecreaseWarning(), "fieldValuesDecreaseWarning not null");
        final Map<String, String> fieldValuesDecreaseWarning = config.getFieldValuesDecreaseWarning();
        assertEquals(2, fieldValuesDecreaseWarning.size());
        assertEquals("25%", fieldValuesDecreaseWarning.get("field1"), "fieldValuesDecreaseWarning for field1");
        assertEquals("100", fieldValuesDecreaseWarning.get("field2"), "fieldValuesDecreaseWarning for field2");

        assertNotNull(config.getFieldValuesDecreaseError(), "fieldValuesDecreaseError not null");
        final Map<String, String> fieldValuesDecreaseError = config.getFieldValuesDecreaseError();
        assertEquals(1, fieldValuesDecreaseError.size());
        assertEquals("50%", fieldValuesDecreaseError.get("field1"), "fieldValuesDecreaseError for field1");
    }

    @Test
    public void testGetRules() {
        //Note: rule configuration is loaded from properties (see @TestPropertySource annotation)
        Collection<Rule> fieldRules = rules.getRules();
        assertNotNull(fieldRules);
        assertThat(fieldRules, hasSize(5));

        assertThat("Field value count rules", fieldRules, allOf(
                hasItem(allOf(
                        isA(RatioDecreaseRule.class),
                        hasProperty("scope", equalTo(RuleScope.FIELD_VALUE_COUNT)),
                        hasProperty("level", equalTo(Level.WARN)),
                        hasProperty("thresholdRatio", equalTo(.25))
                )),
                hasItem(allOf(
                        isA(RatioDecreaseRule.class),
                        hasProperty("scope", equalTo(RuleScope.FIELD_VALUE_COUNT)),
                        hasProperty("level", equalTo(Level.ERROR)),
                        hasProperty("thresholdRatio", equalTo(.50))
                )),
                hasItem(allOf(
                        isA(AbsoluteDecreaseRule.class),
                        hasProperty("scope", equalTo(RuleScope.FIELD_VALUE_COUNT)),
                        hasProperty("level", equalTo(Level.WARN)),
                        hasProperty("thresholdDiff", equalTo(Long.valueOf(100)))
                ))));

        assertThat("Total record count rules", fieldRules, allOf(
                hasItem(allOf(
                        isA(AbsoluteDecreaseRule.class),
                        hasProperty("scope", equalTo(RuleScope.TOTAL_RECORD_COUNT)),
                        hasProperty("level", equalTo(Level.WARN)),
                        hasProperty("thresholdDiff", equalTo(Long.valueOf(1000)))
                )),
                hasItem(allOf(
                        isA(RatioDecreaseRule.class),
                        hasProperty("scope", equalTo(RuleScope.TOTAL_RECORD_COUNT)),
                        hasProperty("level", equalTo(Level.ERROR)),
                        hasProperty("thresholdRatio", equalTo(.25))
                )))
        );
    }

    @Test
    public void testCreateRulesEmpty() {
        // Empty map
        final Stream<Rule> result = rules.createFieldRules(Level.WARN, Optional.empty());
        assertEquals(0L, result.count());
    }

    @Test
    public void testCreateRulesAbsolute() {
        // Absolute thresholds
        List<Rule> result = rules.createFieldRules(Level.WARN, Optional.of(ImmutableMap.<String, String>builder()
                .put("field1", "1")
                .put("field2", "2")
                .put("field3", "3")
                .build())).collect(Collectors.toList());
        assertEquals(3, result.size());
        assertThat(result, everyItem(isA(AbsoluteDecreaseRule.class)));
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
        List<Rule> result = rules.createFieldRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                .put("field1", "1%")
                .put("field2", "2%")
                .put("field3", " 3%")
                .build())).collect(Collectors.toList());
        assertEquals(3, result.size());
        assertThat(result, everyItem(isA(RatioDecreaseRule.class)));
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
        assertThrows(RuntimeException.class, () -> {
            rules.createFieldRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                    .put("field1", "1")
                    .put("field1", "b")
                    .build()));
        });

        assertThrows(RuntimeException.class, () -> {
            rules.createFieldRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                    .put("field1", "1")
                    .put("field1", "b")
                    .build()));
        });

        assertThrows(RuntimeException.class, () -> {
            rules.createFieldRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                    .put("field1", "1")
                    .put("field1", "2a")
                    .build()));
        });

        assertThrows(RuntimeException.class, () -> {
            rules.createFieldRules(Level.ERROR, Optional.of(ImmutableMap.<String, String>builder()
                    .put("field1", "1%")
                    .put("field1", "2%a")
                    .build()));
        });
    }

    /**
     * Test of getAllFields method, of class RulesConfig.
     */
    @Test
    public void testGetAllFields() {
        final RulesConfig config = new RulesConfig();
        config.setFieldValuesDecreaseWarning(ImmutableMap.of("field1", "0", "field2", "0"));
        config.setFieldValuesDecreaseError(ImmutableMap.of("field1", "0", "field3", "0"));

        RulesService instance = new RulesService(config);

        Collection<String> result = instance.getAllFields();
        assertEquals(3, result.size());
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2"));
        assertTrue(result.contains("field3"));
    }

}
