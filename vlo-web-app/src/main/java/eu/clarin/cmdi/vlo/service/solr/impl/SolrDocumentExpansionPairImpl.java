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

import eu.clarin.cmdi.vlo.service.solr.SolrDocumentExpansionPair;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SolrDocumentExpansionPairImpl implements SolrDocumentExpansionPair, Serializable {
    
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
    public Optional<SolrDocumentList> getExpansionDocuments() {
        if (expansion == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(document.getFieldValue(keyField))
                    .map(signature -> expansion.get(signature.toString()));
        }
    }
    
    @Override
    public long getExpansionCount() {
        return getExpansionDocuments()
                .map(docList -> docList.getNumFound())
                .orElse(Long.valueOf(0));
    }
    
}
