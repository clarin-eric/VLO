/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.service.solr;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import org.apache.solr.client.solrj.SolrQuery;

/**
 *
 * @author twagoo
 */
public interface SolrDocumentQueryFactory {

    /**
     * Creates a query to retrieve all document that match a query/facet
     * selection with an offset and limit
     *
     * @param selection selection criteria
     * @param first search result offset
     * @param count limits the number of results
     * @return a query set up to retrieve the matching documents, starting with
     * the specified starting index and limited to the specified count
     */
    SolrQuery createDocumentQuery(QueryFacetsSelection selection, int first, int count);

    /**
     * Creates a query to retrieve all document that match a query/facet
     * selection with an offset and limit, including result expansion
     *
     * @param selection selection criteria
     * @param first search result offset
     * @param count limits the number of results
     * @return a query set up to retrieve the matching documents, starting with
     * the specified starting index and limited to the specified count
     */
    SolrQuery createDocumentQueryWithExpansion(QueryFacetsSelection selection, int first, int count);

    /**
     * Creates a query to retrieve a single document by id
     *
     * @param docId identifier of document to retrieve
     * @return a query set up to retrieve one row at most with the document that
     * has the specified identifier
     * @see FacetConstants#FIELD_ID
     */
    SolrQuery createDocumentQuery(String docId);

    SolrQuery createDuplicateDocumentsQuery(String docId, String collapseField, String collapseValue, int expansionLimit);

    /**
     * Creates a query to retrieve documents similar to the identified one
     *
     * @param docId identifier of document to retrieve similar alternatives to
     * @return a query set up to retrieve the configured number of similar
     * documents
     */
    SolrQuery createSimilarDocumentsQuery(String docId);

}
