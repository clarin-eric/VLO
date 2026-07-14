/*
 * Copyright (C) 2026 CLARIN
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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.service.solr.MorgueDocumentService;
import java.util.Optional;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads removed-record information from the morgue Solr core.
 */
public class MorgueDocumentServiceImpl implements MorgueDocumentService {

    private final static Logger logger = LoggerFactory.getLogger(MorgueDocumentServiceImpl.class);

    private final SolrClient morgueSolrClient;
    private final VloConfig vloConfig;
    private final String idField;

    /**
     * @param morgueSolrClient client for the morgue core
     * @param vloConfig configuration (for read-only credentials)
     * @param fieldNameService used to resolve the identifier field name
     */
    public MorgueDocumentServiceImpl(SolrClient morgueSolrClient, VloConfig vloConfig, FieldNameService fieldNameService) {
        this.morgueSolrClient = morgueSolrClient;
        this.vloConfig = vloConfig;
        this.idField = fieldNameService.getFieldName(FieldKey.ID);
    }

    @Override
    public Optional<SolrDocument> getById(String docId) {
        if (docId == null) {
            return Optional.empty();
        }
        try {
            final SolrQuery query = new SolrQuery();
            query.setQuery(idField + ":" + ClientUtils.escapeQueryChars(docId));
            // only the first match is ever used; numFound still reports the true
            // total so the "multiple found" warning below remains accurate
            query.setRows(1);

            final QueryRequest req = new QueryRequest(query);
            req.setBasicAuthCredentials(vloConfig.getSolrUserReadOnly(), vloConfig.getSolrUserReadOnlyPass());
            final QueryResponse response = req.process(morgueSolrClient);

            final SolrDocumentList docs = response.getResults();
            if (docs.getNumFound() >= 1) {
                if (docs.getNumFound() > 1) {
                    logger.warn("Found multiple morgue documents for id {} (returning first)", docId);
                }
                return Optional.of(docs.getFirst());
            }
            return Optional.empty();
        } catch (Exception e) {
            // never let a morgue failure break the regular not-found handling
            logger.warn("Could not look up record {} in morgue index", docId, e);
            return Optional.empty();
        }
    }

}
