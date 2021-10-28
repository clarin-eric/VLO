package eu.clarin.cmdi.vlo.api;

import eu.clarin.cmdi.vlo.data.model.VloRecord;
import eu.clarin.cmdi.vlo.elasticsearch.VloRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories;
import org.springframework.http.MediaType;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableReactiveElasticsearchRepositories(basePackageClasses = VloRecordRepository.class)
@Slf4j
public class VloApiApplication {

    public static void main(String[] args) {
        log.info("VLO API: starting");
        SpringApplication.run(VloApiApplication.class, args);
        log.info("VLO API: started");
    }

    @Bean
    public RouterFunction<ServerResponse> route(VloMappingHandler mappingHandler, VloRecordHandler recordHandler) {
        return RouterFunctions
                .route(POST("/recordMapping/request").and(accept(MediaType.APPLICATION_JSON)), mappingHandler::requestMapping)
                .andRoute(GET("/recordMapping/result/{id}").and(accept(MediaType.APPLICATION_JSON)), mappingHandler::getMappingResult)
                .andRoute(GET("/records").and(accept(MediaType.APPLICATION_JSON)), recordHandler::getRecords)
                .andRoute(GET("/record/{id}").and(accept(MediaType.APPLICATION_JSON)), recordHandler::getRecordFromRepository)
                .andRoute(PUT("/record").and(accept(MediaType.APPLICATION_JSON)), recordHandler::saveRecord);
    }

    @Bean
    public VloRecord createTestRecord(VloRecordRepository repo) {
        Mono<VloRecord> switchIfEmpty = repo
                .findById("999")
                .switchIfEmpty(repo.save(VloRecord.builder().id("999").selflink("https://clarin.eu/test.cmdi").build()));
        return switchIfEmpty.block();
    }

}
