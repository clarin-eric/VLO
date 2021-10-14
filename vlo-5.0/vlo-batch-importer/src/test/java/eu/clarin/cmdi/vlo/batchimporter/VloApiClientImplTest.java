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
package eu.clarin.cmdi.vlo.batchimporter;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class VloApiClientImplTest {

    private static MockWebServer mockWebServer;
    private static WebClient webClient;
    private VloApiClientImpl instance;

    public VloApiClientImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        mockWebServer = new MockWebServer();
        webClient = WebClient.create("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort());
    }

    @AfterAll
    public static void tearDownClass() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    public void setUp() {
        instance = new VloApiClientImpl(webClient);
    }

    private Optional<VloRecordMappingProcessingTicket> instanceSendRecordMappingRequest() throws IOException {
        final VloRecordMappingRequest importRequest = VloRecordMappingRequest.builder()
                        .dataRoot("testRoot")
                        .file("/foo/bar.xml")
                        .xmlContent("<cmd></cmd>".getBytes())
                        .build();
        final Mono<VloRecordMappingProcessingTicket> response = instance.sendRecordMappingRequest(importRequest);
        return response.blockOptional(Duration.ofSeconds(10));
    }

    /**
     * Test of sendImportRequest method, of class VloApiClient.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSendRecordMappingRequestEmptyResponse() throws Exception {
        //prepare response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(""));
        
        //make request
        final Optional<VloRecordMappingProcessingTicket> result = instanceSendRecordMappingRequest();

        assertTrue(result.isEmpty());
    }

    /**
     * Test of sendImportRequest method, of class VloApiClient.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSendRecordMappingRequestNonEmptyResponse() throws Exception {
        //prepare response
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{"
                        + "\"file\": \"/foo/bar.xml\","
                        + "\"processId\": \"http://service/recordMapping/xyz\""
                        + "}"));

        //make request
        final Optional<VloRecordMappingProcessingTicket> result = instanceSendRecordMappingRequest();

        result.ifPresentOrElse(resultObj -> {
            assertEquals("/foo/bar.xml", resultObj.getFile());
            assertEquals("http://service/recordMapping/xyz", resultObj.getProcessId());
        }, () -> fail("Expected VloImportProcessingTicket result but none found"));
    }

    private Optional<VloRecord> instanceRetrieveRecord() throws IOException {
        final VloRecordMappingProcessingTicket ticket = VloRecordMappingProcessingTicket.builder()
                .file("/foo/bar.xml")
                .processId("1234-abcd")
                .build();
        final Mono<VloRecord> response = instance.retrieveRecord(ticket);
        return response.blockOptional(Duration.ofSeconds(10));
    }

    /**
     * Test of sendImportRequest method, of class VloApiClient.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testRetrieveRecordNonEmptyResponse() throws Exception {
        //prepare response
        final MockResponse mockResponse = new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{"
                        + "\"id\": \"record1\","
                        + "\"name\": \"bar\""
                        + "}");
        mockWebServer.enqueue(mockResponse);

        //make request
        final Optional<VloRecord> result = instanceRetrieveRecord();
        
        result.ifPresentOrElse(resultObj -> {
            assertEquals("record1", resultObj.getId());
            assertEquals("bar", resultObj.getName());
        }, () -> fail("Expected VloRecord result but none found"));
    }

}
