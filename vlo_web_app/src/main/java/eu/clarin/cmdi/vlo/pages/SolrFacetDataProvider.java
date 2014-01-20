package eu.clarin.cmdi.vlo.pages;

import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.dao.DaoLocator;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;

public class SolrFacetDataProvider extends SortableDataProvider<FacetField, String> {

    private static final long serialVersionUID = 1L;
    private final SolrQuery query;
    private List<FacetField> facets;

    public SolrFacetDataProvider(SolrQuery query) {
        this.query = query;
        query.setFacet(true).setStart(0).setRows(0); //only get facets
    }

    private SearchResultsDao getSearchResultsDao() {
        return DaoLocator.getSearchResultsDao();
    }

    @Override
    public Iterator<? extends FacetField> iterator(long first, long count) {
        return facets.iterator();
    }

    private List<FacetField> getFacets() {
        if (facets == null) {
            SearchResultsDao searchResultsDao = getSearchResultsDao();
            facets = searchResultsDao.getFacets(query);
        }
        return facets;
    }

    @Override
    public IModel<FacetField> model(FacetField facetField) {
        return new Model<FacetField>(facetField);
    }

    @Override
    public long size() {
        return (int) getFacets().size();
    }

}
