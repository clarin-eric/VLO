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

public class SolrFacetDataProvider extends SortableDataProvider<FacetField> {

    private static final long serialVersionUID = 1L;
    private final SolrQuery query;

    public SolrFacetDataProvider(SolrQuery query) {
        this.query = query;
    }

    private SearchResultsDao getSearchResultsDao() {
        return DaoLocator.getSearchResultsDao();
    }

    @Override
    public Iterator<? extends FacetField> iterator(int first, int count) {
        SearchResultsDao searchResultsDao = getSearchResultsDao();
        query.setStart(first).setRows(count);
        List<FacetField> facets = searchResultsDao.getFacets(query);
        return facets.iterator();
    }

    @Override
    public IModel<FacetField> model(FacetField facetField) {
        return new Model<FacetField>(facetField);
    }

    @Override
    public int size() {
        return (int) getSearchResultsDao().getFacets(query).size();
    }

}
