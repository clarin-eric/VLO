package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.monitor.Rules;
import eu.clarin.cmdi.vlo.monitor.Rules.RuleScope;
import eu.clarin.cmdi.vlo.monitor.RulesConfig;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import org.slf4j.event.Level;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class IndexStateCompareServiceImplTest {

    public IndexStateCompareServiceImplTest() {
    }

    final Calendar calendar = Calendar.getInstance();

    private IndexState oldState;
    private IndexState newState;

    private ArrayList<FacetState> newStateFacetStates;
    private ArrayList<FacetState> oldStateFacetStates;

    private HashMap<String, String> fieldValuesDecreaseWarning;
    private HashMap<String, String> fieldValuesDecreaseError;

    private RulesConfig rulesConfig;
    private Rules rules;
    private IndexStateCompareServiceImpl instance;

    @BeforeEach
    public void setUp() {
        oldState = new IndexState();
        calendar.set(2020, 12, 1, 12, 0, 0);
        oldState.setTimestamp(calendar.getTime());
        oldStateFacetStates = Lists.newArrayList();
        oldState.setFacetStates(oldStateFacetStates = Lists.newArrayList());

        newState = new IndexState();
        calendar.set(2020, 12, 1, 13, 0, 0);
        newState.setTimestamp(calendar.getTime());
        newState.setFacetStates(newStateFacetStates = Lists.newArrayList());

        rulesConfig = new RulesConfig();
        rulesConfig.setFieldValuesDecreaseError(fieldValuesDecreaseError = Maps.newHashMap());
        rulesConfig.setFieldValuesDecreaseWarning(fieldValuesDecreaseWarning = Maps.newHashMap());
        rules = new Rules(rulesConfig);

        instance = new IndexStateCompareServiceImpl();
    }

    /**
     * Test of compare method, of class IndexStateCompareServiceImpl.
     */
    @Test
    public void testCompareNoChange() {
        //// field 1 rule
        fieldValuesDecreaseWarning.put("field1", "10");

        // Add value with small decrease
        oldStateFacetStates.add(new FacetState("field1", "val1a", 1000L));
        newStateFacetStates.add(new FacetState("field1", "val1a", 999L)); // -1
        assertThat(instance.compare(oldState, newState, rules), hasSize(0));

        // Add value with increase
        oldStateFacetStates.add(new FacetState("field1", "val1b", 1000L));
        newStateFacetStates.add(new FacetState("field1", "val1b", 1001L)); // +1
        assertThat(instance.compare(oldState, newState, rules), hasSize(0));

        // Add field for which there is no rule
        oldStateFacetStates.add(new FacetState("field2", "val2a", 1000L));
        newStateFacetStates.add(new FacetState("field2", "val2a", 0L)); // -100%
        assertThat(instance.compare(oldState, newState, rules), hasSize(0));

        // Add rule for which there is no field
        fieldValuesDecreaseWarning.put("field3", "1%");
        assertThat(instance.compare(oldState, newState, rules), hasSize(0));

        //add total record count rules
        rulesConfig.setTotalRecordsDecreaseWarning("150");
        rulesConfig.setTotalRecordsDecreaseError("15%");

        // record counts
        oldState.setTotalRecordCount(1000L);
        newState.setTotalRecordCount(900L);
    }

    /**
     * Test of compare method, of class IndexStateCompareServiceImpl.
     */
    @Test
    public void testCompareWithChanges() {
        // set up rules & states

        //// field 1 rules
        fieldValuesDecreaseWarning.put("field1", "10"); //will be triggered for 1 value
        fieldValuesDecreaseError.put("field1", "20"); // will not be triggered

        // field 1 value 1a
        oldStateFacetStates.add(new FacetState("field1", "val1a", 1000L));
        newStateFacetStates.add(new FacetState("field1", "val1a", 985L)); // -15 -> WARNING

        // field 1 value 1b
        oldStateFacetStates.add(new FacetState("field1", "val1b", 1000L));
        newStateFacetStates.add(new FacetState("field1", "val1b", 1005L)); // -5

        //// field 2 rules
        fieldValuesDecreaseWarning.put("field2", "10%"); // will be triggered for 2 values
        fieldValuesDecreaseError.put("field2", "20%"); // will be triggered for 1 value

        // field 2 value 2a
        oldStateFacetStates.add(new FacetState("field2", "val2a", 1000L));
        newStateFacetStates.add(new FacetState("field2", "val2a", 900L)); // -10% -> WARNING

        // field 2 value 2b
        oldStateFacetStates.add(new FacetState("field2", "val2b", 1000L));
        newStateFacetStates.add(new FacetState("field2", "val2b", 750L)); // -25% -> WARNING & ERROR

        //// field 3 rules
        fieldValuesDecreaseWarning.put("field3", "50%"); // will not be triggered

        //field 3 value 3a
        oldStateFacetStates.add(new FacetState("field3", "val3a", 1000L));
        newStateFacetStates.add(new FacetState("field3", "val3a", 1000L));

        //total record count rules
        rulesConfig.setTotalRecordsDecreaseWarning("50%");
        rulesConfig.setTotalRecordsDecreaseError("600");

        // record counts
        oldState.setTotalRecordCount(1000L);
        newState.setTotalRecordCount(500L);

        final Collection<MonitorReportItem> result = instance.compare(oldState, newState, rules);

        assertThat(result, hasSize(5));

        assertThat(result, hasItem(allOf(
                hasProperty("field", equalTo(Optional.of("field1"))),
                hasProperty("value", equalTo(Optional.of("val1a"))),
                hasProperty("level", equalTo(Level.WARN))
        )));
        assertThat(result, hasItem(allOf(
                hasProperty("field", equalTo(Optional.of("field2"))),
                hasProperty("value", equalTo(Optional.of("val2a"))),
                hasProperty("level", equalTo(Level.WARN))
        )));
        assertThat(result, hasItem(allOf(
                hasProperty("field", equalTo(Optional.of("field2"))),
                hasProperty("value", equalTo(Optional.of("val2b"))),
                hasProperty("level", equalTo(Level.WARN))
        )));
        assertThat(result, hasItem(allOf(
                hasProperty("field", equalTo(Optional.of("field2"))),
                hasProperty("value", equalTo(Optional.of("val2b"))),
                hasProperty("level", equalTo(Level.ERROR))
        )));
        assertThat(result, hasItem(allOf(
                hasProperty("rule", hasProperty("scope", equalTo(RuleScope.TOTAL_RECORD_COUNT))),
                hasProperty("level", equalTo(Level.WARN))
        )));
    }

    /**
     * Test of compare method, of class IndexStateCompareServiceImpl.
     */
    @Test
    public void testCompareWithValuesDisappear() {
        // set up rules & states

        //// field 1 rule
        fieldValuesDecreaseWarning.put("field1", "500");

        // field 1 value 1a
        oldStateFacetStates.add(new FacetState("field1", "val1a", 1000L));
        newStateFacetStates.add(new FacetState("field1", "val1a", 100L)); // -900 -> WARNING

        // field 1 value 1b
        oldStateFacetStates.add(new FacetState("field1", "val1b", 1000L));
        // -------------------------------------------------------------- // value disappeared -> WARNING

        // field 2 rule
        fieldValuesDecreaseError.put("field2", "1000");

        // field 2 value 2a
        oldStateFacetStates.add(new FacetState("field2", "val2a", 1000L));
        newStateFacetStates.add(new FacetState("field2", "val2a", 0L)); // -1000 -> ERROR

        // field 2 value 2b
        oldStateFacetStates.add(new FacetState("field2", "val2b", 1000L));
        // -------------------------------------------------------------- // value disappeared -> ERROR

        // field 3 rule
        fieldValuesDecreaseError.put("field3", "100%");

        // field 3 value 3a
        oldStateFacetStates.add(new FacetState("field3", "val3a", 1000L));
        newStateFacetStates.add(new FacetState("field3", "val3a", 0L)); // -100% -> ERROR

        // field 3 value 3b
        oldStateFacetStates.add(new FacetState("field3", "val3b", 1000L));
        // -------------------------------------------------------------- // value disappeared -> ERROR        

        final Collection<MonitorReportItem> result = instance.compare(oldState, newState, rules);

        assertThat(result, hasSize(6));

        assertThat(result, hasItem(
                allOf(
                        hasProperty("field", equalTo(Optional.of("field1"))),
                        hasProperty("value", equalTo(Optional.of("val1a"))),
                        hasProperty("level", equalTo(Level.WARN))
                )
        ));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("field", equalTo(Optional.of("field1"))),
                        hasProperty("value", equalTo(Optional.of("val1b"))),
                        hasProperty("level", equalTo(Level.WARN))
                )
        ));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("field", equalTo(Optional.of("field2"))),
                        hasProperty("value", equalTo(Optional.of("val2a"))),
                        hasProperty("level", equalTo(Level.ERROR))
                )
        ));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("field", equalTo(Optional.of("field2"))),
                        hasProperty("value", equalTo(Optional.of("val2b"))),
                        hasProperty("level", equalTo(Level.ERROR))
                )
        ));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("field", equalTo(Optional.of("field3"))),
                        hasProperty("value", equalTo(Optional.of("val3a"))),
                        hasProperty("level", equalTo(Level.ERROR))
                )
        ));
        assertThat(result, hasItem(
                allOf(
                        hasProperty("field", equalTo(Optional.of("field3"))),
                        hasProperty("value", equalTo(Optional.of("val3b"))),
                        hasProperty("level", equalTo(Level.ERROR))
                )
        ));
    }

    /**
     * Test of compare method, of class IndexStateCompareServiceImpl.
     */
    @Test
    public void testCompareWithValuesAppear() {
        // set up rules & states

        //// field 1 rule
        fieldValuesDecreaseWarning.put("field1", "5%");

        // field 1 value 1a
        // -------------------------------------------------------------- // value did not exist before
        newStateFacetStates.add(new FacetState("field1", "val1a", 100L));

        //// field 2 rule
        fieldValuesDecreaseWarning.put("field2", "500");

        // field 2 value 2a
        // -------------------------------------------------------------- // value did not exist before
        oldStateFacetStates.add(new FacetState("field2", "val2a", 0L));
        newStateFacetStates.add(new FacetState("field2", "val2a", 100L));

        final Collection<MonitorReportItem> result = instance.compare(oldState, newState, rules);

        assertThat(result, hasSize(0));
    }
}
