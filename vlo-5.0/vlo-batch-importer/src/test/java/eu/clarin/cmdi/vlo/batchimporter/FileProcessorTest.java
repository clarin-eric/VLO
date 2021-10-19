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

import eu.clarin.cmdi.vlo.data.model.MetadataFile;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingProcessingTicket;
import eu.clarin.cmdi.vlo.data.model.VloRecordMappingRequest;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
@ExtendWith(MockitoExtension.class)
public class FileProcessorTest extends AbstractVloImporterTest {
    
    @Mock
    VloApiClient apiClient;
    
    @Captor
    ArgumentCaptor<VloRecordMappingRequest> mappingReqCaptor;
    @Captor
    ArgumentCaptor<VloRecordMappingProcessingTicket> processingTicketCaptor;

    /**
     * Test of process method, of class FileProcessor.
     */
    @Test
    public void testProcess() throws Exception {
        MetadataFile inputFile = new MetadataFile("testRoot", getTestResource("test_record1.xml"));
        FileProcessor instance = new FileProcessor(apiClient);
        
        final VloRecordMappingProcessingTicket ticket = mock(VloRecordMappingProcessingTicket.class);
        when(apiClient.sendRecordMappingRequest(any(VloRecordMappingRequest.class)))
                .thenReturn(Mono.just(ticket));
        final VloRecord record = mock(VloRecord.class);
        when(apiClient.retrieveRecord(any(VloRecordMappingProcessingTicket.class)))
                .thenReturn(Mono.just(record));
        
        final VloRecord result = instance.process(inputFile).block(Duration.ofSeconds(60));
        assertEquals(record, result);
        
        verify(apiClient, times(1)).sendRecordMappingRequest(mappingReqCaptor.capture());
        assertEquals("testRoot", mappingReqCaptor.getValue().getDataRoot());
        assertTrue(mappingReqCaptor.getValue().getFile().contains("test_record1.xml"));
        
        verify(apiClient, times(1)).retrieveRecord(processingTicketCaptor.capture());
        assertEquals(ticket, processingTicketCaptor.getValue());
    }
    
}
