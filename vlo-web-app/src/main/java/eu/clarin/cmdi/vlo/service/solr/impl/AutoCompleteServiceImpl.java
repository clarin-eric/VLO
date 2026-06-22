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

/**
 * DAO that delivers suggestions for incomplete terms (autocomplete function)
 *
 * @author Thomas Eckart
 * @author Twan Goosen
 */
public class AutoCompleteServiceImpl extends SolrDaoImpl implements AutoCompleteService {

    public AutoCompleteServiceImpl(SolrClient solrClient, VloConfig config, FieldNameService fieldNameService) {
        super(solrClient, config, fieldNameService);
    }

    @Override
    public Iterator<String> getChoices(String input) {
        if (input == null) {
            return Collections.emptyIterator();
        }
        final SolrQuery query = new SolrQuery();
        query.setQuery(input.toLowerCase());
        final QueryRequest req = new QueryRequest(query);
        req.setPath("/suggest");
        // fireRawQuery bypasses QueryResponse which crashes on the "suggest" key
        // with solrj 9 + Solr 8 (LinkedHashMap vs NamedList cast)
        return parseSuggestions(fireRawQuery(req));
    }

    private Iterator<String> parseSuggestions(NamedList<Object> raw) {
        if (!(raw.get("suggest") instanceof Map<?, ?> suggest) || suggest.isEmpty()) {
            return Collections.emptyIterator();
        }
        if (!(suggest.values().iterator().next() instanceof Map<?, ?> dict) || dict.isEmpty()) {
            return Collections.emptyIterator();
        }
        if (!(dict.values().iterator().next() instanceof Map<?, ?> result)) {
            return Collections.emptyIterator();
        }
        if (!(result.get("suggestions") instanceof List<?> suggestions)) {
            return Collections.emptyIterator();
        }
        return suggestions.stream()
                .filter(s -> s instanceof Map<?, ?> m && m.get("term") instanceof String)
                .map(s -> (String) ((Map<?, ?>) s).get("term"))
                .iterator();
    }
}
