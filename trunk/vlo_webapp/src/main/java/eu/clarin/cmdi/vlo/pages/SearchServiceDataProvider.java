package eu.clarin.cmdi.vlo.pages;

import java.util.Iterator;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.dao.DaoLocator;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;

/**
 * Data provider of all documents with SearchService entry (FCS) based on existing SolrQuery
 * 
 * @author Thomas Eckart
 *
 */
public class SearchServiceDataProvider extends SortableDataProvider<SolrDocument> {
	private final static Logger LOG = LoggerFactory.getLogger(SearchServiceDataProvider.class);
	
	private static final long serialVersionUID = -5355607690141772113L;
	private final SolrQuery query;
	private SolrDocumentList docList;

	public SearchServiceDataProvider(SolrQuery query) {
		this.query = query;
		this.query.setFacet(false);
		this.query.setStart(0);
		this.query.setRows(10);
		this.query.setFields(FacetConstants.FIELD_SEARCH_SERVICE, FacetConstants.FIELD_ID);
		this.query.setQuery(query.getQuery());
		this.query.addFilterQuery(FacetConstants.FIELD_SEARCH_SERVICE + ":*");
		LOG.debug("Used query for search services: "+this.query.toString());
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
	public Iterator<SolrDocument> iterator(int first, int count) {
		if (first != query.getStart().intValue() || count != query.getRows().intValue()) {
			query.setStart(first).setRows(count);
			docList = null;
		}
		return getDocList().iterator();
	}
	
	public Iterator<SolrDocument> iterator() {
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
