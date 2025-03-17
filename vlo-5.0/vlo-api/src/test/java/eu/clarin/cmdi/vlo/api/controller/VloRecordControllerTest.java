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

import eu.clarin.cmdi.vlo.api.model.VloRequest;
import eu.clarin.cmdi.vlo.api.service.VloRecordService;
import eu.clarin.cmdi.vlo.api.service.impl.FilterMapFactoryImpl;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private ArgumentCaptor<VloRequest> recordsRequestCaptor;
    @Captor
    private ArgumentCaptor<String> idCaptor;
    @Captor
    private ArgumentCaptor<VloRecord> saveRequestCaptor;

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

        final VloRequest recordsRequest = recordsRequestCaptor.getValue();
        assertEquals("*:*", recordsRequest.getQuery());
    }
    
    
    @Test
    public void getRecordCount() throws Exception {
        when(recordService.getRecordCount(Mockito.any(), Mockito.any()))
                .thenReturn(42L);

        mvc.perform(MockMvcRequestBuilders
                .get("/records/count")
                .accept(MediaType.ALL))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getRecord() throws Exception {
        when(recordService.getRecordById(idCaptor.capture()))
                .thenReturn(Optional.of(createVloRecord(r -> {
                    r.setId("id1");
                })));

        mvc.perform(MockMvcRequestBuilders
                .get("/records/id1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
        //TODO: expect more content
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("id1"));
        
        final String requestId = idCaptor.getValue();
        assertEquals("id1", requestId);
    }

    @Test
    public void getRecordNotFound() throws Exception {
        when(recordService.getRecordById(idCaptor.capture()))
                .thenReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders
                .get("/records/id1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        final String requestId = idCaptor.getValue();
        assertEquals("id1", requestId);
    }

    @Test
    public void testSaveRecord() throws Exception {
        final String recordJson
                = """
                  {
                    "selflink": "selflink1",
                    "profileId": "profile1",
                    "fields": { 
                        "field1": ["value1"] 
                    }
                  }
                  """;

        when(recordService.saveRecord(saveRequestCaptor.capture()))
                .thenReturn(Optional.of(createVloRecord(r -> {
                    r.setId("id1");
                    r.setProfileId("profile1");
                })));

        mvc.perform(MockMvcRequestBuilders
                .post("/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
        //TODO: expectations on response

        final VloRecord reqRecord = saveRequestCaptor.getValue();
        assertThat(reqRecord, allOf(
                hasProperty("selflink", equalTo("selflink1")),
                hasProperty("profileId", equalTo("profile1")),
                hasProperty("fields", allOf(
                        is(aMapWithSize(1)),
                        hasEntry(
                                is("field1"),
                                allOf(
                                        iterableWithSize(1),
                                        hasItem(equalTo("value1"))
                                ))))));
    }

    @Test
    public void testSaveRecordInvalidContent() throws Exception {
        final String recordJson
                = """
                  {
                    "foo": 
                  """;

        mvc.perform(MockMvcRequestBuilders
                .post("/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(recordService, never()).saveRecord(Mockito.any());
    }

    @Test
    public void testSaveRecordServiceError() throws Exception {
        final String recordJson = "{}";

        when(recordService.saveRecord(Mockito.any()))
                .thenReturn(Optional.empty());

        mvc.perform(MockMvcRequestBuilders
                .post("/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(recordJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    private static VloRecordSearchResult createSearchResult(long numFound, long start, Consumer<VloRecord>... recordInitialisers) {
        final Stream<VloRecord> records = Stream.of(recordInitialisers)
                .map(VloRecordControllerTest::createVloRecord);

        return new VloRecordSearchResult(records.toList(), numFound, start);
    }

    private static VloRecord createVloRecord(Consumer<VloRecord> init) {
        final VloRecord record = new VloRecord();
        //apply initialiser
        init.accept(record);
        return record;
    }

}
