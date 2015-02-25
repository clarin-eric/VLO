package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;

/**
 * DAO that delivers suggestions for incomplete terms (autocomplete function)
 *
 * @author Thomas Eckart
 * @author Twan Goosen
 *
 */
public class AutoCompleteServiceImpl extends SolrDaoImpl implements AutoCompleteService {

    public AutoCompleteServiceImpl(SolrServer solrServer, VloConfig config) {
        super(solrServer, config);
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
            
            if (response.getSpellCheckResponse() != null) {
                final List<Suggestion> suggestions = response.getSpellCheckResponse().getSuggestions();
                if (suggestions.size() > 0) {
                    return suggestions.get(0).getAlternatives().iterator();
                }
            }
        }

        return  Collections.emptyIterator();
    }
}
