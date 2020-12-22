package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.monitor.VloMonitorConfiguration;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import eu.clarin.cmdi.vlo.monitor.model.MonitorReportItem;
import eu.clarin.cmdi.vlo.monitor.model.Rule;
import static eu.clarin.cmdi.vlo.monitor.model.Rule.RuleScope.FIELD_VALUE_COUNT;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.DateUtils;
import static org.assertj.core.api.Assumptions.assumeThat;
import org.assertj.core.util.Lists;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.AdditionalMatchers.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.event.Level;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@ExtendWith(MockitoExtension.class)
public class VloMonitorTest {

    @Spy
    private VloMonitorConfiguration config;

    @Mock
    private IndexService indexService;

    @Mock
    private IndexStateRepository repo;

    @Mock
    private IndexStateCompareService compareService;

    @Mock
    private RulesService rules;
    
    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private VloMonitor instance;

    private final AtomicLong indexStateId = new AtomicLong(System.currentTimeMillis());

    private List<IndexState> indexStates;

    @BeforeEach
    public void beforeEach() {
        indexStates = createIndexStates();
    }

    @Captor
    ArgumentCaptor<IndexState> indexStateCaptor;
    
    @Captor
    ArgumentCaptor<Collection<MonitorReportItem>> reportCaptor;

    @Test
    public void testRun() {
        assumeThat(instance).isNotNull();

        config.setPruneAfterDays(Optional.empty());

        final long newTotalRecordCount = 1000L;
        final IndexState previousIndexState = indexStates.get(0);
        previousIndexState.setTotalRecordCount(2000L);
        previousIndexState.setFacetStates(ImmutableList.of(new FacetState("field1", "value1a", 100L)));

        final Rule rule = Rule.create(FIELD_VALUE_COUNT, Level.WARN, Optional.of("field1"), "10%");
        final Collection<MonitorReportItem> result = ImmutableList.of(
                new MonitorReportItem(rule, Optional.of("value1a"), "message")
        );

        when(rules.getAllFields())
                .thenReturn(Lists.newArrayList("field1", "field2", "field3"));
        when(indexService.getTotalRecordCount())
                .thenReturn(newTotalRecordCount);

        when(indexService.getValueCounts("field1")).thenReturn(ImmutableMap.of("value1a", 100L, "value1b", 110L));
        when(indexService.getValueCounts("field2")).thenReturn(ImmutableMap.of("value2a", 200L));
        when(indexService.getValueCounts("field3")).thenReturn(ImmutableMap.of("value3a", 300L));

        when(repo.findFirstByOrderByTimestampDesc())
                .thenReturn(Optional.of(previousIndexState));
        when(compareService.compare(any(IndexState.class), any(IndexState.class)))
                .thenReturn(result);

        instance.run();
        
        // verify retrieval of input

        verify(indexService, atLeast(1)).getTotalRecordCount();
        verify(indexService, atLeast(3)).getValueCounts(any(String.class));
        
        // verify comparison
        
        verify(compareService, times(1)).compare(eq(previousIndexState), indexStateCaptor.capture());

        final IndexState newIndex = indexStateCaptor.getValue();
        assertNotNull(newIndex);
        assertNotNull(newIndex.getFacetStates());
        assertThat(newIndex.getFacetStates(), hasSize(4));
        assertThat(newIndex.getFacetStates(), hasItem(
                allOf(
                        hasProperty("facet", equalTo("field1")),
                        hasProperty("val", equalTo("value1a")),
                        hasProperty("count", equalTo(100L))
                )
        ));
        
        // verify reporting of results        

        verify(reportingService, times(1)).report(reportCaptor.capture());
        assertEquals(result, reportCaptor.getValue());
        
        // verify storing of new state

        verify(repo, times(1)).save(indexStateCaptor.capture());
        
        final IndexState savedIndex = indexStateCaptor.getValue();
        assertNotNull(savedIndex);
        assertNotNull(savedIndex.getFacetStates());
        assertEquals(4, savedIndex.getFacetStates().size());
        assertEquals(newTotalRecordCount, savedIndex.getTotalRecordCount());
    }

    /**
     * Test of repo pruning.
     */
    @Test
    public void testPruning() {
        assumeThat(instance).isNotNull();

        final int pruneDaysAfter = 30;
        final Date dateLimit = DateUtils.addDays(new Date(), -1 * pruneDaysAfter);
        final List<IndexState> statesToBePruned
                = indexStates
                        .stream()
                        .filter(i -> i.getTimestamp().before(dateLimit)).collect(Collectors.toList());

        config.setPruneAfterDays(Optional.of(pruneDaysAfter));

        when(rules.getAllFields())
                .thenReturn(Lists.emptyList());
        when(repo.findFirstByOrderByTimestampDesc())
                .thenReturn(Optional.of(indexStates.get(0)));
        when(repo.findOlderThan(any(Date.class)))
                .thenReturn(statesToBePruned);

        instance.run();

        verify(repo).findOlderThan(argThat((date) -> {
            return Math.abs(date.getTime() - dateLimit.getTime()) < 60000;
        }));

        verify(repo).deleteAll(and(anyList(), argThat((list) -> {
            return ((List) list).size() == statesToBePruned.size();
        })));

    }

    private ImmutableList<IndexState> createIndexStates() {
        return ImmutableList.<IndexState>builder()
                .add(createIndexState(Calendar.HOUR, 1, Lists.emptyList()))
                .add(createIndexState(Calendar.HOUR, 12, Lists.emptyList()))
                .add(createIndexState(Calendar.DAY_OF_MONTH, 2, Lists.emptyList()))
                .add(createIndexState(Calendar.DAY_OF_MONTH, 10, Lists.emptyList()))
                .add(createIndexState(Calendar.DAY_OF_MONTH, 20, Lists.emptyList()))
                .add(createIndexState(Calendar.DAY_OF_MONTH, 50, Lists.emptyList()))
                .add(createIndexState(Calendar.DAY_OF_MONTH, 100, Lists.emptyList()))
                .build();
    }

    private IndexState createIndexState(int ageUnit, int ageDuration, List<FacetState> facetState) {
        final IndexState indexState = new IndexState();
        indexState.setId(indexStateId.getAndAdd(1));

        final Calendar calendar = Calendar.getInstance();
        calendar.add(ageUnit, -1 * ageDuration);
        indexState.setTimestamp(calendar.getTime());

        indexState.setFacetStates(facetState);
        return indexState;
    }

}
