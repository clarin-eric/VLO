package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO that delivers suggestions for incomplete terms (autocomplete function)
 *
 * @author Thomas Eckart
 * @author Twan Goosen
 */
public class AutoCompleteServiceImpl extends SolrDaoImpl implements AutoCompleteService {

    private static final Logger logger = LoggerFactory.getLogger(AutoCompleteServiceImpl.class);

    public AutoCompleteServiceImpl(SolrClient solrClient, VloConfig config, FieldNameService fieldNameService) {
        super(solrClient, config, fieldNameService);
    }

    @Override
    public Iterator<String> getChoices(String input) {
        if (input != null) {
            final SolrQuery query = new SolrQuery();
            query.setQuery(input.toLowerCase());

            final QueryRequest req = new QueryRequest(query);
            req.setPath("/suggest");

            // Use fireRawQuery to bypass QueryResponse which crashes on the "suggest" key
            // when using solrj 9 client against a Solr 8 server (LinkedHashMap vs NamedList cast).
            // "suggest" is a LinkedHashMap, inner objects are SimpleOrderedMap — use Map for all levels.
            final NamedList<Object> raw = fireRawQuery(req);

            @SuppressWarnings("unchecked")
            final Map<String, Object> suggestSection = (Map<String, Object>) raw.get("suggest");
            if (suggestSection != null) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> dictionary = (Map<String, Object>) suggestSection.values().iterator().next();
                @SuppressWarnings("unchecked")
                final Map<String, Object> queryResult = (Map<String, Object>) dictionary.values().iterator().next();
                @SuppressWarnings("unchecked")
                final List<Map<String, Object>> suggestions = (List<Map<String, Object>>) queryResult.get("suggestions");
                if (suggestions != null && !suggestions.isEmpty()) {
                    return suggestions.stream()
                            .map(s -> (String) s.get("term"))
                            .iterator();
                }
            }
        }

        return Collections.emptyIterator();
    }
}
