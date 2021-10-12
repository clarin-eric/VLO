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

import eu.clarin.cmdi.vlo.data.model.VloImportProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloImportRequest;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@Slf4j
public class VloApiClientTest {

    private static MockWebServer mockWebServer;
    private static WebClient webClient;
    private VloApiClient instance;

    public VloApiClientTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        mockWebServer = new MockWebServer();
        webClient = WebClient.create("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort());
    }

    @BeforeEach
    public void setUp() {
        instance = new VloApiClient(webClient);
    }

    /**
     * Test of sendImportRequest method, of class VloApiClient.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSendImportRequestEmptyResponse() throws Exception {
        VloImportRequest importRequest = createImportRequest();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(""));

        final Optional<VloImportProcessingTicket> result = instance.sendImportRequest(importRequest).blockOptional(Duration.ofSeconds(10));
        assertTrue(result.isEmpty());
    }

    /**
     * Test of sendImportRequest method, of class VloApiClient.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSendImportRequestNonEmptyResponse() throws Exception {
        VloImportRequest importRequest = createImportRequest();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{"
                        + "\"file\": \"/foo/bar.xml\","
                        + "\"resourceUri\": \"http://service/recordMapping/xyz\""
                        + "}"));

        final Optional<VloImportProcessingTicket> result = instance.sendImportRequest(importRequest).blockOptional(Duration.ofSeconds(10));
        result.ifPresentOrElse(resultObj -> {
            assertEquals("/foo/bar.xml", resultObj.getFile());
            assertEquals("http://service/recordMapping/xyz", resultObj.getResourceUri());
        }, () -> fail("Expected VloImportProcessingTicket result but none found"));
    }

    private VloImportRequest createImportRequest() {
        final VloImportRequest importRequest
                = VloImportRequest.builder()
                        .dataRoot("testRoot")
                        .file("/foo/bar.xml")
                        .xmlContent("<cmd></cmd>".getBytes())
                        .build();
        return importRequest;
    }

}
