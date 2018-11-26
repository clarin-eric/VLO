/*
 * Copyright (C) 2018 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionList;
import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SolrDocumentExpansionListImpl implements SolrDocumentExpansionList, Serializable {

    private static final SolrDocumentExpansionList EMPTY = new EmptySolrDocumentExpansionList();

    private final QueryResponse queryResponse;
    private final String collapseField;

    public SolrDocumentExpansionListImpl(QueryResponse queryResponse, String collapseField) {
        this.queryResponse = queryResponse;
        this.collapseField = collapseField;
    }

    @Override
    public List<SolrDocumentExpansionPair> getDocuments() {
        return getPairStream().collect(Collectors.toList());
    }

    @Override
    public Iterator<? extends SolrDocumentExpansionPair> iterator() {
        return getPairStream().iterator();
    }

    private Stream<SolrDocumentExpansionPair> getPairStream() {
        final Map<String, SolrDocumentList> expansion = queryResponse.getExpandedResults();
        return queryResponse.getResults()
                .stream()
                .map(doc -> new SolrDocumentExpansionPairImpl(doc, expansion, collapseField));
    }

    @Override
    public long getNumFound() {
        return queryResponse.getResults().getNumFound();
    }

    static SolrDocumentExpansionList empty() {
        return EMPTY;
    }


    private final static class EmptySolrDocumentExpansionList implements SolrDocumentExpansionList, Serializable {

        @Override
        public List<SolrDocumentExpansionPair> getDocuments() {
            return Collections.emptyList();
        }

        @Override
        public long getNumFound() {
            return 0L;
        }

        @Override
        public Iterator<? extends SolrDocumentExpansionPair> iterator() {
            return IteratorUtils.emptyIterator();
        }
    }

}
