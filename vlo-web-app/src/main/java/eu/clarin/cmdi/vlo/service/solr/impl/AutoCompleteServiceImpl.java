package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.service.solr.AutoCompleteService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.ArrayList;
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
     * @return list of suggestions
     */
    @Override
    public List<String> getChoices(String input) {
        List<String> choices = new ArrayList<String>();

        if (input != null) {
            SolrQuery query = new SolrQuery();
            query.setQuery(input.toLowerCase());
            query.setQueryType("/suggest");
            QueryResponse response = fireQuery(sanitise(query));
            if (response.getSpellCheckResponse() != null) {
                List<Suggestion> suggestions = response.getSpellCheckResponse().getSuggestions();
                if (suggestions.size() > 0) {
                    Iterator<String> iter = suggestions.get(0).getAlternatives().iterator();
                    while (iter.hasNext()) {
                        choices.add(iter.next());
                    }
                }
            }
        }

        return choices;
    }
}
