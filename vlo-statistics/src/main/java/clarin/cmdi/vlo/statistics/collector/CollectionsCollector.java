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
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class CollectionsCollector implements VloStatisticsCollector {

    private final static Logger logger = LoggerFactory.getLogger(CollectionsCollector.class);
    
    private FieldNameService fieldNameService = null;

    @Override
    public void collect(VloReport report, VloConfig config, SolrClient solrClient) throws SolrServerException, IOException {
        if(this.fieldNameService == null)
            this.fieldNameService = new FieldNameServiceImpl(config);
        
        report.setCollections(obtainCollectionCounts(config, solrClient));
    }

    private List<VloReport.CollectionCount> obtainCollectionCounts(VloConfig config, SolrClient solrClient) throws SolrServerException, IOException {
        final SolrQuery query = new SolrQuery();
        query.setRows(0);
        query.setFacet(true);
        query.addFacetField(fieldNameService.getFieldName(FieldKey.COLLECTION));
        query.setFacetLimit(Integer.MAX_VALUE);

        QueryRequest req = new QueryRequest(query);
        req.setBasicAuthCredentials(config.getSolrUserReadOnly(), config.getSolrUserReadOnlyPass());
        final QueryResponse result = req.process(solrClient);
        final FacetField collectionField = result.getFacetField(fieldNameService.getFieldName(FieldKey.COLLECTION));
        logger.debug("Collection field: {}", collectionField.getValues());

        final List<VloReport.CollectionCount> counts
                = collectionField.getValues().stream().map((count) -> {
                    VloReport.CollectionCount collectionCount = new VloReport.CollectionCount();
                    collectionCount.setCollection(count.getName());
                    collectionCount.setCount(count.getCount());
                    return collectionCount;
                }).collect(Collectors.toList());
        return counts;
    }
}
