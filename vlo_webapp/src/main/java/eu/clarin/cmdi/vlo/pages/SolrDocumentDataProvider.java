package eu.clarin.cmdi.vlo.pages;

import java.util.Iterator;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.dao.DaoLocator;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;

public class SolrDocumentDataProvider extends SortableDataProvider<SolrDocument> {

    private static final long serialVersionUID = 1L;
    private final SolrQuery query;

    public SolrDocumentDataProvider(SolrQuery query) {
        this.query = query;
    }

    private SearchResultsDao getSearchResultsDao() {
        return DaoLocator.getSearchResultsDao();
    }

    @Override
    public Iterator<? extends SolrDocument> iterator(int first, int count) {
        SearchResultsDao searchResultsDao = getSearchResultsDao();
        query.setStart(first).setRows(count);
        return searchResultsDao.getResults(query).iterator();
    }

    @Override
    public IModel<SolrDocument> model(SolrDocument solrDocument) {
        return new Model<SolrDocument>(solrDocument);
    }

    @Override
    public int size() {
        return (int) getSearchResultsDao().getResults(query).getNumFound();
    }

}
