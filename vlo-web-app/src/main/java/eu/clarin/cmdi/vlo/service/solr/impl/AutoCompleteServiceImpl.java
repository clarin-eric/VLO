package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * DAO that delivers suggestions for incomplete terms (autocomplete function)
 *
 * @author Thomas Eckart
 * @author Twan Goosen
 *
 */
public class AutoCompleteServiceImpl extends SolrDaoImpl implements AutoCompleteService {

    public AutoCompleteServiceImpl(SolrClient solrClient, VloConfig config, FieldNameService fieldNameService) {
        super(solrClient, config, fieldNameService);
    }
    
    /**
     * Returns list of suggestions for incomplete input (used for autocomplete
     * function)
     *
     * @param input user input
     * @return iterator over suggestions
     */
    @Override
    public Iterator<String> getChoices(String input) {
        if (input != null) {
            
            final SolrQuery query = new SolrQuery();
            query.setQuery(input.toLowerCase());
            query.setRequestHandler("/suggest");
            
            final QueryResponse response = fireQuery(sanitise(query));
            
            if (response.getSuggesterResponse()!= null) {
                final Map<String, List<String>> suggestions = response.getSuggesterResponse().getSuggestedTerms();
                if (!suggestions.isEmpty()) {
                    return suggestions.get(new ArrayList<>(suggestions.keySet()).get(0)).iterator();
                }
            }
        }

        return  Collections.emptyIterator();
    }
}
