/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.vlo.sitemap.services;

import eu.clarin.vlo.sitemap.gen.Config;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
class SolrHelpers {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrHelpers.class);

    static QueryResponse getSolrResult(SolrClient solrClient, SolrQuery recordsQuery) throws RuntimeException {
        try {
            QueryResponse response = solrClient.query(recordsQuery);
            return response;
        } catch (SolrServerException | IOException ex) {
            LOGGER.error("Fatal exception while querying index", ex);
            throw new RuntimeException(ex);
        }
    }

    static SolrClient createSolrClient(final String solrUrl) {
        LOGGER.info("Initializing Solr client on {}", solrUrl);
        return new HttpJdkSolrClient.Builder(solrUrl)
                .withBasicAuthCredentials(Config.SOLR_USER, Config.SOLR_PASS)
                .build();
    }

}
