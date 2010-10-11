package eu.clarin.cmdi.vlo.pages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.params.CommonParams;
import org.apache.wicket.PageParameters;

import eu.clarin.cmdi.vlo.Configuration;

public class SearchPageQuery implements Serializable {

    private static final String SOLR_SEARCH_ALL = "*:*";

    private static final long serialVersionUID = 1L;

    private SolrQuery query;
    private Map<String, String> filterQueryMap = new HashMap<String, String>();
    private String searchQuery;

    public SearchPageQuery(PageParameters parameters) {
        query = getDefaultQuery();
        query.setQuery(parameters.getString(CommonParams.Q, SOLR_SEARCH_ALL));
        String[] filterQueries = parameters.getStringArray(CommonParams.FQ);
        query.setFilterQueries(filterQueries);
        init();
    }

    public SearchPageQuery(SolrQuery query) {
        this.query = query;
        init();
    }

    private void init() {
        searchQuery = query.getQuery();
        String[] filterQueries = query.getFilterQueries();
        if (filterQueries != null) {
            for (String fq : filterQueries) {
                String[] keyValue = fq.split(":");
                filterQueryMap.put(keyValue[0], keyValue[1]);
            }
        }
    }

    public SearchPageQuery getShallowCopy() {
        return new SearchPageQuery(query);
    }

    private SolrQuery getDefaultQuery() {
        SolrQuery result = new SolrQuery();
        result.setRows(10);
        result.setStart(0);
        result.setFields("name", "id");
        result.setFacet(true);
        result.setFacetMinCount(1);
        result.addFacetField(Configuration.getInstance().getFacetFields());
        return result;
    }

    public SolrQuery getSolrQuery() {
        return query;
    }

    public void removeFilterQuery(FacetField facetField) {
        filterQueryMap.remove(facetField.getName());
    }

    public void setFilterQuery(Count count) {
        filterQueryMap.put(count.getFacetField().getName(), count.getName());
    }

    public boolean isSelected(FacetField facetField) {
        return filterQueryMap.containsKey(facetField.getName());
    }

    public String getSelectedValue(FacetField field) {
        return filterQueryMap.get(field.getName());
    }

    public void setSearchQuery(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            searchQuery = SOLR_SEARCH_ALL;
        }
        query.setQuery(searchQuery);
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery() {
        if (searchQuery.equals(SOLR_SEARCH_ALL)) {
            searchQuery = "";
        }
        return searchQuery;
    }

    public PageParameters getPageParameters() {
        PageParameters result = new PageParameters();
        if (!getSearchQuery().isEmpty()) {
            result.add(CommonParams.Q, getSearchQuery());
        }
        for (String facet : filterQueryMap.keySet()) {
            String facetValue = filterQueryMap.get(facet);
            result.add(CommonParams.FQ, facet + ":" + facetValue);
        }
        return result;
    }

}
