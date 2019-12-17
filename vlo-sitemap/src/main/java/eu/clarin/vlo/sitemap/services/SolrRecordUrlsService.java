package eu.clarin.vlo.sitemap.services;

import com.google.common.collect.Lists;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.vlo.sitemap.gen.Config;
import eu.clarin.vlo.sitemap.pojo.Sitemap.URL;
import static eu.clarin.vlo.sitemap.services.SolrHelpers.createSolrClient;
import static eu.clarin.vlo.sitemap.services.SolrHelpers.getSolrResult;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrRecordUrlsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrRecordUrlsService.class);
    private static final String ID_FIELD = "id";

    private final Integer solrRequestPageSize;
    private final SolrClient solrClient;

    public SolrRecordUrlsService() {
        solrRequestPageSize = Config.SOLR_REQUEST_PAGE_SIZE;
        solrClient = createSolrClient(Config.SOLR_URL);
    }

    public List<URL> getRecordURLS() {
        final List<Stream<String>> idStreams = collectRecordIdentifiers();
        return createRecordURLs(idStreams);
    }

    private List<Stream<String>> collectRecordIdentifiers() {
        // collect ids
        final List<Stream<String>> idStreams = Collections.synchronizedList(Lists.newArrayList());
        queryAndProcess(createIdsQuery(), (response) -> {
            final SolrDocumentList result = response.getResults();

            final int pageSize = result.size();
            LOGGER.debug("Records retrieved", pageSize);

            //update all of the documents (parallel stream)
            idStreams.add(result.parallelStream().map(r -> r.getFirstValue(ID_FIELD).toString()));
        });
        return idStreams;
    }

    private SolrQuery createIdsQuery() throws RuntimeException, IllegalArgumentException {
        // Prepare Solr query
        final SolrQuery recordsQuery = new SolrQuery("*:*");
        recordsQuery.setRequestHandler(FacetConstants.SOLR_REQUEST_HANDLER_FAST);

        if (LOGGER.isInfoEnabled()) {
            // Get result count
            recordsQuery.setRows(0);
            LOGGER.info("Found {} documents", getSolrResult(solrClient, recordsQuery).getResults().getNumFound());
        }

        // Prepare query for processing
        recordsQuery.setRows(solrRequestPageSize);
        recordsQuery.setFields(ID_FIELD);
        recordsQuery.setSort(SolrQuery.SortClause.asc(ID_FIELD));
        return recordsQuery;
    }

    private void queryAndProcess(final SolrQuery recordsQuery, Consumer<QueryResponse> resultPageConsumer) throws RuntimeException {
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;

        while (!done) {
            LOGGER.info("Requesting result page from Solr. Maximum number of rows: {}", recordsQuery.getRows());
            LOGGER.debug("Query: {}", recordsQuery.toString());

            recordsQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
            final QueryResponse response = getSolrResult(solrClient, recordsQuery);

            resultPageConsumer.accept(response);

            done = (cursorMark.equals(response.getNextCursorMark()));
            cursorMark = response.getNextCursorMark();
        }
    }

    private List<URL> createRecordURLs(List<Stream<String>> idStreams) {
        // transform into list of record page URLs
        return idStreams.stream()
                //map list of ID lists to one dimensional lists
                .flatMap(s -> s)
                //make URLS for all IDs
                .map(id -> new URL(Config.RECORD_URL_TEMPLATE + id))
                .collect(Collectors.toList());
    }

}
