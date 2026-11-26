package eu.clarin.cmdi.vlo.monitor.service;

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.springframework.stereotype.Service;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
@Service
@Slf4j
public class IndexServiceImpl implements IndexService {

    @Inject
    private VloConfig config;

    private SolrClient solrClient;

    @Override
    public Map<String, Long> getValueCounts(String facet) {
        final SolrQuery q = new SolrQuery("*:*")
                .setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST)
                .addFacetField(facet)
                .setRows(0);

        final QueryResponse response = query(q);

        final ImmutableMap.Builder<String, Long> mapBuilder = ImmutableMap.builder();
        response.getFacetFields().stream()
                // find counts for target field
                .filter(f -> facet.equals(f.getName()))
                // get values (=list of counts)
                .map(f -> f.getValues())
                // there should be only 1 per field
                .limit(1)
                // put counts in our map
                .forEach((values) -> {
                    values.forEach(count -> {
                        mapBuilder.put(count.getName(), count.getCount());
                    });
                });
        return mapBuilder.build();
    }

    @Override
    public Long getTotalRecordCount() {
        final SolrQuery q
                = new SolrQuery("*:*")
                        .setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST)
                        .setRows(0);
        return query(q).getResults().getNumFound();
    }

    private QueryResponse query(SolrParams params) {
        try {
            return solrClient.query(params);
        } catch (SolrServerException | IOException ex) {
            log.error("Error while querying the Solr index", ex);
            throw new RuntimeException(ex);
        }
    }

    @PostConstruct
    protected void initSolrClient() {
        final String solrUrl = config.getSolrUrl();
        log.info("Initializing Solr client on {}", solrUrl);
        solrClient = new HttpJdkSolrClient.Builder(solrUrl)
                .withBasicAuthCredentials(config.getSolrUserReadWrite(), config.getSolrUserReadWritePass())
                .build();
    }

    @PreDestroy
    protected void closeSolrClient() {
        try {
            solrClient.close();
        } catch (IOException ex) {
            log.error("Error while closing Solr client", ex);
        }
    }

}
