package eu.clarin.cmdi.vlo.pages;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.params.FacetParams;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.vlo.dao.DaoLocator;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;

public class SolrFacetFieldDataProvider implements IDataProvider<Count> {
    private final static Logger LOG = LoggerFactory.getLogger(SolrFacetFieldDataProvider.class);

    private static final long serialVersionUID = 1L;
    private FacetField facet;
    private SolrQuery query;

    private final String selectedFacet;

    public SolrFacetFieldDataProvider(String selectedFacet, SearchPageQuery pageQuery) {
        this.selectedFacet = selectedFacet;
        query = pageQuery.getSolrQuery();
        query.setFacetSort(FacetParams.FACET_SORT_INDEX);
        query.setFacetLimit(-1);
        String[] facetFields = query.getFacetFields();
        for (String facetField : facetFields) {
            if (!facetField.equals(selectedFacet)) {
                query.removeFacetField(facetField);
            }
        }

    }

    private FacetField getFacet() {
        if (facet == null) {
            SearchResultsDao searchResultsDao = DaoLocator.getSearchResultsDao();
            List<FacetField> facets = searchResultsDao.getFacets(query);
            if (facets == null || facets.isEmpty()) {
                facet = new EmptyFacetField(selectedFacet);
            } else if (facets.size() != 1) {
                LOG.warn("Should have only one facet, but got: " + facets.size() + ". Choosing first one.");
            } else {
                facet = facets.get(0);
            }
        }
        return facet;
    }

    @Override
    public Iterator<? extends Count> iterator(int first, int count) {
        return getFacet().getValues().subList(first, first + count).iterator();
    }

    @Override
    public int size() {
        return getFacet().getValueCount();
    }

    @Override
    public IModel<Count> model(Count count) {
        return new Model<Count>(count);
    }

    @Override
    public void detach() {
    }

    @SuppressWarnings("serial")
    private static class EmptyFacetField extends FacetField {
        public EmptyFacetField(String name) {
            super(name);
        }

        public List<Count> getValues() {
            return (List<Count>) Collections.EMPTY_LIST.iterator();
        }

        public int getValueCount() {
            return 0;
        }

    }
}
