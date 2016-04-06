/*
 * Copyright (C) 2016 CLARIN
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
package clarin.cmdi.vlo.statistics.collector;

import clarin.cmdi.vlo.statistics.model.VloReport;
import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class RecordCountCollector implements VloStatisticsCollector {

    @Override
    public void collect(VloReport report, VloConfig config, SolrServer solrServer) throws SolrServerException {
        final SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setRows(0);
        final QueryResponse result = solrServer.query(query);
        final long recordCount = result.getResults().getNumFound();

        report.setRecordCount(recordCount);

    }

}
