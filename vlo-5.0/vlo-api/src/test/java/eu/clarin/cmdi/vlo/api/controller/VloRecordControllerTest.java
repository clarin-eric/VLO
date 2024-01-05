/*
 * Copyright (C) 2024 twagoo
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
package eu.clarin.cmdi.vlo.api.controller;

import eu.clarin.cmdi.vlo.api.model.VloRecordsRequest;
import eu.clarin.cmdi.vlo.api.service.VloRecordService;
import eu.clarin.cmdi.vlo.api.service.impl.FilterMapFactoryImpl;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import java.util.function.Consumer;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST service test: /records
 *
 * @author twagoo
 */
@ActiveProfiles("test")
@WebMvcTest(controllers = VloRecordController.class)
@Import(FilterMapFactoryImpl.class)
public class VloRecordControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VloRecordService recordService;

    @Captor
    private ArgumentCaptor<VloRecordsRequest> recordsRequestCaptor;

    private final static VloRecordSearchResult TWO_RECORDS_RESULT
            = createSearchResult(2, 0,
                    record -> {
                        record.setId("id1");
                        record.setSelflink("selflink1");
                    },
                    record -> {
                        record.setId("id2");
                        record.setSelflink("selflink2");
                    }
            );

    private final ResultMatcher[] TWO_RECORDS_RESULT_MATCHERS = {
        MockMvcResultMatchers.jsonPath("$.records").exists(),
        MockMvcResultMatchers.jsonPath("$.numFound").value("2"),
        MockMvcResultMatchers.jsonPath("$.start").value("0"),
        MockMvcResultMatchers.jsonPath("$.records[*].id").isNotEmpty()
    };

    @Test
    public void getRecordsWithoutParams() throws Exception {
        when(recordService.getRecords(recordsRequestCaptor.capture()))
                .thenReturn(TWO_RECORDS_RESULT);

        mvc.perform(MockMvcRequestBuilders
                .get("/records")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(TWO_RECORDS_RESULT_MATCHERS);

        final VloRecordsRequest recordsRequest = recordsRequestCaptor.getValue();
        assertEquals("*:*", recordsRequest.getQuery());
    }

    @Test
    public void getRecordsWithParams() throws Exception {
        when(recordService.getRecords(recordsRequestCaptor.capture()))
                .thenReturn(TWO_RECORDS_RESULT);

        mvc.perform(MockMvcRequestBuilders
                .get("/records?q=test&from=1&size=5&fq=field1:val1&fq=field2:val2")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(TWO_RECORDS_RESULT_MATCHERS);

        final VloRecordsRequest recordsRequest = recordsRequestCaptor.getValue();
        assertEquals("test", recordsRequest.getQuery());
        assertEquals(5, recordsRequest.getSize());
        assertEquals(1, recordsRequest.getFrom());
        assertNotNull(recordsRequest.getFilters());
        assertEquals(2, recordsRequest.getFilters().size());
        assertTrue(recordsRequest.getFilters().containsKey("field1"));
        assertTrue(recordsRequest.getFilters().containsKey("field2"));
        assertThat(recordsRequest.getFilters().get("field1"), is(iterableWithSize(1)));
        assertThat(recordsRequest.getFilters().get("field1"), hasItem("val1"));
    }

    private static VloRecordSearchResult createSearchResult(long numFound, long start, Consumer<VloRecord>... recordInitialisers) {
        final Stream<VloRecord> records = Stream.of(recordInitialisers)
                .map(init -> {
                    //create new record
                    final VloRecord record = new VloRecord();
                    //apply initialiser
                    init.accept(record);
                    return record;
                });

        return new VloRecordSearchResult(records.toList(), numFound, start);
    }

}
