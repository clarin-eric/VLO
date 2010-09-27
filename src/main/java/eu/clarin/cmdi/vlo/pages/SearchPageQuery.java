package eu.clarin.cmdi.vlo.pages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

public class SearchPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private SolrQuery query;
    private Map<String, String> filterqueryMap = new HashMap<String, String>();

    public SearchPageQuery(String queryParam) {
        //http://localhost:8983/solr/select/?wt=json&indent=on&q=*:*&fl=name&facet=true&facet.field=cat&facet.field=inStock
        //TODO PD test for encoding etc etcc, maybe don't encapulate the solr params inside on request param? 
        query = new SolrQuery();
        String[] params = queryParam.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            query.add(keyValue[0], keyValue[1]);
        }
        String[] filterQueries = query.getFilterQueries();
        if (filterQueries != null) {
            for (String fq : filterQueries) {
                String[] fqSplit = fq.split(":");
                String facet = fqSplit[0];
                filterqueryMap.put(facet, fqSplit[1]);
            }
        }
    }

    public SearchPageQuery(SolrQuery query) {
        this.query = query;
    }

    public static SearchPageQuery getDefaultQuery() {
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(10);
        query.setStart(0);
        query.setFields("name", "id");
        query.setFacet(true);
        query.addFacetField("continent", "organisation", "country", "language", "genre");//, "subject", "description");
        return new SearchPageQuery(query);
    }

    public SolrQuery getSolrQuery() {
        return query;
    }

    public void setFilterQuery(Count count) {
        filterqueryMap.put(count.getFacetField().getName(), count.getName());
        setFilterQuery();
    }

    private void setFilterQuery() {
        query.setFilterQueries((String[]) null);
        for (String facet : filterqueryMap.keySet()) {
            query.addFilterQuery(facet + ":" + filterqueryMap.get(facet));
        }
    }

    public void removeFilterQuery(FacetField facetField) {
        filterqueryMap.remove(facetField.getName());
        setFilterQuery();
    }

    public boolean isSelected(FacetField facetField) {
        return filterqueryMap.containsKey(facetField.getName());
    }

    public String getSelectedValue(FacetField field) {
        return filterqueryMap.get(field.getName());
    }

}
