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
import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FacetValueCountsCollector implements VloStatisticsCollector {

    @Override
    public void collect(VloReport report, VloConfig config, SolrServer solrServer) throws SolrServerException {
        report.setFacets(obtainFacetStats(config, solrServer));
    }
    
    

    private List<VloReport.Facet> obtainFacetStats(VloConfig config, SolrServer solrServer) throws SolrServerException {
        final SolrQuery query = new SolrQuery();
        query.setRows(0);
        query.setFacet(true);
        FacetConstants.AVAILABLE_FACETS.forEach((field) -> {
            query.addFacetField(field);
        });
        query.setFacetLimit(-1);

        final QueryResponse result = solrServer.query(query);
        final List<FacetField> facetFields = result.getFacetFields();

        final List<VloReport.Facet> facets
                = facetFields.stream().map((field) -> {
                    final VloReport.Facet facet = new VloReport.Facet();
                    facet.setName(field.getName());
                    facet.setValueCount(field.getValueCount());
                    return facet;
                }).collect(Collectors.toList());
        return facets;
    }
    
}
