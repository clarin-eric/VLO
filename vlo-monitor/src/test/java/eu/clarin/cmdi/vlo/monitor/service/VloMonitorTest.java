package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.monitor.Rules;
import eu.clarin.cmdi.vlo.monitor.VloMonitorConfiguration;
import eu.clarin.cmdi.vlo.monitor.model.FacetState;
import eu.clarin.cmdi.vlo.monitor.model.IndexState;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assumptions.assumeThat;
import org.assertj.core.util.Lists;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.Spy;

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
    private Rules rules;

    @InjectMocks
    private VloMonitor instance;

    private static final AtomicLong indexStateId = new AtomicLong(System.currentTimeMillis());

    private static List<IndexState> indexStates;

    @BeforeAll
    public static void beforeAll() {
        indexStates = createIndexStates();
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

        instance.init();

        when(repo.findFirstByOrderByTimestampDesc())
                .thenReturn(Optional.of(indexStates.get(0)));
        when(repo.findOlderThan(any(Date.class)))
                .thenReturn(statesToBePruned);
        instance.run();

        verify(repo).findOlderThan(argThat((date) -> {
            return Math.abs(date.getTime() - dateLimit.getTime()) < 60000;
        }));

        verify(repo).deleteAll(and(anyList(), argThat((list) -> {
            return ((List)list).size() == statesToBePruned.size();
        })));

    }

    private static ImmutableList<IndexState> createIndexStates() {
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

    private static IndexState createIndexState(int ageUnit, int ageDuration, List<FacetState> facetState) {
        final IndexState indexState = new IndexState();
        indexState.setId(indexStateId.getAndAdd(1));

        final Calendar calendar = Calendar.getInstance();
        calendar.add(ageUnit, -1 * ageDuration);
        indexState.setTimestamp(calendar.getTime());

        indexState.setFacetStates(facetState);
        return indexState;
    }

}
