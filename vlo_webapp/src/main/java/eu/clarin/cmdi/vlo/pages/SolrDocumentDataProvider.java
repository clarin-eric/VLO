package eu.clarin.cmdi.vlo.pages;

import java.util.Iterator;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.dao.DaoLocator;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;

public class SolrDocumentDataProvider extends SortableDataProvider<SolrDocument> {

    private static final long serialVersionUID = 1L;
    private final SolrQuery query;
    private SolrDocumentList docList;

    public SolrDocumentDataProvider(SolrQuery query) {
        this.query = query;
        if (query.getSortField() == null) {
            query.setSortField(FacetConstants.FIELD_NAME, SolrQuery.ORDER.asc);
        }
        query.setFacet(false);
    }

    private SearchResultsDao getSearchResultsDao() {
        return DaoLocator.getSearchResultsDao();
    }

    private SolrDocumentList getDocList() {
        if (docList == null) {
            docList = getSearchResultsDao().getResults(query);
        }
        return docList;
    }

    @Override
    public Iterator<? extends SolrDocument> iterator(int first, int count) {
        if (first != query.getStart().intValue() || count != query.getRows().intValue()) {
            query.setStart(first).setRows(count);
            docList = null;
        }
        return getDocList().iterator();
    }

    @Override
    public IModel<SolrDocument> model(SolrDocument solrDocument) {
        return new Model<SolrDocument>(solrDocument);
    }

    @Override
    public int size() {
        return (int) getDocList().getNumFound();
    }

}
