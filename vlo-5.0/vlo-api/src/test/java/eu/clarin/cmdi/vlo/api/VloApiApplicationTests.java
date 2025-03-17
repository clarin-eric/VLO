package eu.clarin.cmdi.vlo.api;

import eu.clarin.cmdi.vlo.api.model.VloRequest;
import eu.clarin.cmdi.vlo.api.service.VloFacetService;
import eu.clarin.cmdi.vlo.api.service.VloRecordService;
import eu.clarin.cmdi.vlo.data.model.Facet;
import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.data.model.VloRecordSearchResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("test")
class VloApiApplicationTests {

    @TestConfiguration
    public static class VloApiApplicationTestsContextConfiguration {

        @Bean
        public VloFacetService facetService() {
            return new VloFacetService() {
                @Override
                public List<Facet> getFacets(VloRequest request) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public Optional<Facet> getFacet(String facet, VloRequest request) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }
            };
        }

        @Bean
        public VloRecordService recordService() {
            return new VloRecordService() {
                @Override
                public Optional<VloRecord> getRecordById(String id) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public Long getRecordCount(String queryParam, Map<String, ? extends Iterable<String>> filters) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public VloRecordSearchResult getRecords(VloRequest request) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }

                @Override
                public Optional<VloRecord> saveRecord(VloRecord record) {
                    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                }
            };
        }
    }

    @Test
    void contextLoads() {
    }

}
