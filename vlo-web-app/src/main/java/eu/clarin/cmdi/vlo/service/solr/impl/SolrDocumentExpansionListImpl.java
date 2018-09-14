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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SolrDocumentExpansionListImpl implements SolrDocumentExpansionList, Serializable {

    private static final SolrDocumentExpansionList EMPTY = new EmptySolrDocumentExpansionList();

    private final QueryResponse queryResponse;

    public SolrDocumentExpansionListImpl(QueryResponse queryResponse) {
        this.queryResponse = queryResponse;
    }

    @Override
    public List<SolrDocumentExpansionPair> getDocuments() {
        final Map<String, SolrDocumentList> expansion = queryResponse.getExpandedResults();
        return queryResponse.getResults()
                .stream()
                .map(doc -> new SolrDocumentExpansionPairImpl(doc, expansion, "_signature"))
                .collect(Collectors.toList());
    }

    @Override
    public long getNumFound() {
        return queryResponse.getResults().getNumFound();
    }

    static SolrDocumentExpansionList empty() {
        return EMPTY;
    }

    private static class SolrDocumentExpansionPairImpl implements SolrDocumentExpansionPair, Serializable {

        private final String keyField;
        private final SolrDocument document;
        private final Map<String, SolrDocumentList> expansion;

        public SolrDocumentExpansionPairImpl(SolrDocument document, Map<String, SolrDocumentList> expansion, String keyField) {
            this.document = document;
            this.expansion = expansion;
            this.keyField = keyField;
        }

        @Override
        public SolrDocument getDocument() {
            return document;
        }

        @Override
        public boolean hasExpansion() {
            return expansion != null;
        }

        @Override
        public SolrDocumentList getExpansionDocuments() {
            return expansion.get(document.getFieldValue(keyField).toString());
        }

        @Override
        public long getExpansionCount() {
            return getExpansionDocuments().getNumFound();
        }

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
    }

}
