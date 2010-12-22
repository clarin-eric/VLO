package eu.clarin.cmdi.vlo.pages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.wicket.PageParameters;

import eu.clarin.cmdi.vlo.Configuration;
import eu.clarin.cmdi.vlo.FacetConstants;

public class SearchPageQuery implements Serializable {

    private static final String SOLR_SEARCH_ALL = "*:*";

    private static final long serialVersionUID = 1L;

    private SolrQuery query;
    private Map<String, String> filterQueryMap = new HashMap<String, String>();
    private String searchQuery;

    public SearchPageQuery(PageParameters parameters) {
        query = getDefaultQuery();
        String queryParam = parameters.getString(CommonParams.Q);
        setSearchQuery(queryParam);
        if (queryParam != null) {
            query.setQuery(escapeSolrQuery(queryParam));
        } else {
            query.setQuery(SOLR_SEARCH_ALL);

        }
        String[] filterQueries = parameters.getStringArray(CommonParams.FQ);
        if (filterQueries != null) {
            String[] encodedQueries = new String[filterQueries.length];
            for (int i = 0; i < filterQueries.length; i++) {
                String fq = filterQueries[i];
                String[] keyValue = fq.split(":", 2);
                filterQueryMap.put(keyValue[0], keyValue[1]);
                encodedQueries[i] = keyValue[0] + ":" + ClientUtils.escapeQueryChars(keyValue[1]);
            }
            query.setFilterQueries(encodedQueries);
        }
    }

    private String escapeSolrQuery(String value) {
        String result = null;
        if (value != null) {
            result = ClientUtils.escapeQueryChars(value);
        }
        return result;
    }

    private SearchPageQuery(SearchPageQuery searchPageQuery) {
        this.query = searchPageQuery.query;
        this.filterQueryMap = new HashMap(searchPageQuery.filterQueryMap);
        this.searchQuery = searchPageQuery.searchQuery;
    }

    public SearchPageQuery getShallowCopy() {
        return new SearchPageQuery(this);
    }

    private SolrQuery getDefaultQuery() {
        SolrQuery result = new SolrQuery();
        result.setRows(10);
        result.setStart(0);
        result.setFields(FacetConstants.FIELD_NAME, FacetConstants.FIELD_ID);
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

    public Map<String, String> getFilterQueryMap() {
        return filterQueryMap;
    }

    public void setSearchQuery(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            searchQuery = SOLR_SEARCH_ALL;
        }
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
