/*
 * Copyright (C) 2021 CLARIN ERIC <clarin@clarin.eu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.web.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.web.service.VloApiClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(MockitoExtension.class)
public class ReactiveVloRecordRepositoryTest {

    @Mock
    private VloApiClient apiClient;

    public ReactiveVloRecordRepositoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testFindAllPageable() {
        when(apiClient.getRecords(any(String.class), any(Optional.class), any(Optional.class)))
                .thenReturn(Flux.fromIterable(
                        ImmutableList.of(
                                createRecord("record1"),
                                createRecord("record2"))));

        final ReactiveVloRecordRepository instance = new ReactiveVloRecordRepository(apiClient);
        final Pageable pageable = PageRequest.of(2, 10);
        final Flux<VloRecord> result = instance.findAll(pageable);
        final Flux<VloRecord> take = result.take(5, true);
        final Iterable<VloRecord> resultAsIterable = take.toIterable();
        final Iterable<VloRecord> resultAsLimitedIterable = Iterables.limit(resultAsIterable, 100);
        final List<VloRecord> resultAsList = ImmutableList.copyOf(resultAsLimitedIterable);
        assertEquals(2, resultAsList.size());

        verify(apiClient, times(1)).getRecords("*", Optional.of(10L), Optional.of(21L)); //start = offset + 1
    }

    private VloRecord createRecord(String id) {
        return VloRecord.builder().id(id).build();
    }

}
