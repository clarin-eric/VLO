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
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies a minimal representation of records that are about to be removed from
 * the main index into the "morgue" index, recording the date of removal. This
 * is to show a tombstone page for removed records instead of a plain "not found"
 * error.
 *
 * <p>
 * The field names of the morgue core match those of the main index
 * (as resolved through the {@link FieldNameService}), so archiving is a simple
 * field-by-field copy plus the addition of a removal timestamp.</p>
 */
public class MorgueIndexer implements Closeable {

    private final static Logger LOG = LoggerFactory.getLogger(MorgueIndexer.class);

    /**
     * Number of records to read/write per batch.
     */
    private static final int BATCH_SIZE = 1000;

    /**
     * Keys of the fields copied verbatim from the main index into the morgue.
     * Each key must have a matching field in the morgue schema.
     */
    private static final FieldKey[] COPIED_KEYS = {
        // record details (title, provider, timestamps)
        FieldKey.ID, FieldKey.NAME, FieldKey.DATA_PROVIDER,
        FieldKey.FIRST_SEEN, FieldKey.LAST_SEEN,
        // links (help the user find the resource elsewhere)
        FieldKey.SELF_LINK, FieldKey.LANDINGPAGE, FieldKey.SEARCHPAGE,
        FieldKey.SEARCH_SERVICE, FieldKey.RESOURCE, FieldKey.RESOURCE_COUNT,
        // technical details
        FieldKey.HARVESTER_ROOT, FieldKey.CLARIN_PROFILE, FieldKey.CLARIN_PROFILE_ID,
        FieldKey.COMPLETE_METADATA, FieldKey.HAS_PART_COUNT
    };

    private final VloConfig config;
    private final FieldNameService fieldNameService;
    private final SolrClient sourceClient;
    private final SolrClient morgueClient;

    /**
     * @param config VLO configuration (provides the morgue URL and credentials)
     * @param fieldNameService used to resolve field names
     * @param sourceClient client for the main index (records are read from here)
     */
    public MorgueIndexer(VloConfig config, FieldNameService fieldNameService, SolrClient sourceClient) {
        this(config, fieldNameService, sourceClient,
                new HttpJdkSolrClient.Builder(config.getSolrMorgueUrl()).build());
    }

    /**
     * Package-private constructor for testing: accepts a pre-built morgue
     * client so tests can inject a fake without starting an HTTP server.
     */
    MorgueIndexer(VloConfig config, FieldNameService fieldNameService,
            SolrClient sourceClient, SolrClient morgueClient) {
        this.config = config;
        this.fieldNameService = fieldNameService;
        this.sourceClient = sourceClient;
        this.morgueClient = morgueClient;
    }

    /**
     * Reads all records from the main index matching the given query and writes a
     * minimal representation of each into the morgue index, stamped with the
     * given removal time.
     *
     * @param query query selecting the records to archive (same query that will
     * be used to delete them)
     * @param removedDate the removal timestamp to record
     * @return the number of records archived
     * @throws SolrServerException
     * @throws IOException
     */
    public int archiveRemovedRecords(String query, Instant removedDate) throws SolrServerException, IOException {
        final Date removedDateValue = Date.from(removedDate);
        final String idField = fieldNameService.getFieldName(FieldKey.ID);

        final SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        solrQuery.setFields(fieldsToFetch());
        solrQuery.setRows(BATCH_SIZE);
        // a deterministic sort on the unique key is required for cursor paging
        solrQuery.setSort(idField, SolrQuery.ORDER.asc);

        return forEachResultPage(sourceClient, solrQuery, FacetConstants.SOLR_REQUEST_HANDLER_FAST, results -> {
            final UpdateRequest updateRequest = new UpdateRequest();
            updateRequest.setBasicAuthCredentials(config.getSolrUserReadWrite(), config.getSolrUserReadWritePass());
            for (SolrDocument doc : results) {
                updateRequest.add(toMorgueDocument(doc, removedDateValue));
            }
            updateRequest.process(morgueClient);
        });
    }

    /**
     * Finds the first seen field value of a tombstone record if it exists
     */
    public Optional<Instant> getFirstSeen(String id) {
        if (id == null) {
            return Optional.empty();
        }
        final String idField = fieldNameService.getFieldName(FieldKey.ID);
        final String firstSeenField = fieldNameService.getFieldName(FieldKey.FIRST_SEEN);
        try {
            final SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(idField + ":" + ClientUtils.escapeQueryChars(id));
            solrQuery.setFields(firstSeenField);
            solrQuery.setRows(1);

            final QueryRequest queryRequest = new QueryRequest(solrQuery);
            queryRequest.setBasicAuthCredentials(config.getSolrUserReadOnly(), config.getSolrUserReadOnlyPass());
            final QueryResponse response = queryRequest.process(morgueClient);

            for (SolrDocument doc : response.getResults()) {
                if (doc.getFieldValue(firstSeenField) instanceof Date firstSeenDate) {
                    return Optional.of(firstSeenDate.toInstant());
                }
            }
            return Optional.empty();
        } catch (SolrServerException | IOException ex) {
            LOG.warn("Could not look up record {} in the morgue index; treating it as newly seen", id, ex);
            return Optional.empty();
        }
    }

    /**
     * Pages through every result of {@code solrQuery} against {@code client}
     * using cursor paging, passing each page of documents to {@code pageHandler}
     * in turn. The query must already have its row count and a deterministic
     * sort on the unique key set (both required for cursor paging).
     *
     * @param client the Solr client to read from
     * @param solrQuery the query to page through
     * @param requestPath the request handler path to route to (e.g. {@code /fast});
     * {@code null} to use the default {@code /select} handler
     * @param pageHandler receives the documents of each page
     * @return the total number of documents visited across all pages
     * @throws SolrServerException
     * @throws IOException
     */
    private int forEachResultPage(SolrClient client, SolrQuery solrQuery, String requestPath, PageHandler pageHandler)
            throws SolrServerException, IOException {
        int processed = 0;
        String cursorMark = CursorMarkParams.CURSOR_MARK_START;
        boolean done = false;
        while (!done) {
            solrQuery.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

            final QueryRequest queryRequest = new QueryRequest(solrQuery);
            if (requestPath != null) {
                queryRequest.setPath(requestPath);
            }
            queryRequest.setBasicAuthCredentials(config.getSolrUserReadOnly(), config.getSolrUserReadOnlyPass());
            final QueryResponse response = queryRequest.process(client);

            final List<SolrDocument> results = response.getResults();
            processed += results.size();
            // the final cursor page (and a zero-hit query) yields no documents; skip it
            if (!results.isEmpty()) {
                pageHandler.handle(results);
            }

            final String nextCursorMark = response.getNextCursorMark();
            if (nextCursorMark == null || nextCursorMark.equals(cursorMark)) {
                done = true;
            }
            cursorMark = nextCursorMark;
        }
        return processed;
    }

    /**
     * Callback for {@link #forEachResultPage}, invoked once per page of results.
     */
    @FunctionalInterface
    private interface PageHandler {

        void handle(List<SolrDocument> page) throws SolrServerException, IOException;
    }

    /**
     * Reconciles the morgue with the live index: every record whose id is present
     * in <em>both</em> cores is a record that was purged (hence archived) and has
     * since been re-added, so its tombstone is now stale and needs to be removed.
     *
     * <p>
     * NOTE: this scans the morgue on every import (id-only, in batches). This can
     * be quite expensive. If it proves to be too much then we can gather the newly
     * imported records and limit the checks for those alone.
     * </p>
     *
     * @return the number of stale tombstones removed
     * @throws SolrServerException
     * @throws IOException
     */
    public int removeTombstonesForLiveRecords() throws SolrServerException, IOException {
        final String idField = fieldNameService.getFieldName(FieldKey.ID);

        final SolrQuery morgueQuery = new SolrQuery();
        morgueQuery.setQuery("*:*");
        morgueQuery.setFields(idField);
        morgueQuery.setRows(BATCH_SIZE);
        morgueQuery.setSort(idField, SolrQuery.ORDER.asc);

        final int[] removed = {0};
        forEachResultPage(morgueClient, morgueQuery, null, page -> {
            final List<String> morgueIds = new ArrayList<>(page.size());
            for (SolrDocument doc : page) {
                final Object id = doc.getFieldValue(idField);
                if (id != null) {
                    morgueIds.add(id.toString());
                }
            }
            final List<String> liveIds = idsPresentInLiveIndex(morgueIds, idField);
            if (!liveIds.isEmpty()) {
                final UpdateRequest deleteRequest = new UpdateRequest();
                deleteRequest.setBasicAuthCredentials(config.getSolrUserReadWrite(), config.getSolrUserReadWritePass());
                deleteRequest.deleteById(liveIds);
                deleteRequest.process(morgueClient);
                removed[0] += liveIds.size();
            }
        });
        return removed[0];
    }

    /**
     * Returns which of the given ids currently exist in the live index.
     */
    private List<String> idsPresentInLiveIndex(List<String> ids, String idField) throws SolrServerException, IOException {
        if (ids.isEmpty()) {
            return List.of();
        }
        final String orClause = ids.stream()
                .map(ClientUtils::escapeQueryChars)
                .collect(Collectors.joining(" OR ", idField + ":(", ")"));

        final SolrQuery liveItemsQuery = new SolrQuery();
        liveItemsQuery.setQuery(orClause);
        liveItemsQuery.setFields(idField);
        liveItemsQuery.setRows(ids.size());

        final QueryRequest queryRequest = new QueryRequest(liveItemsQuery);
        // read the live index through the 'fast' handler, which does not collapse results
        queryRequest.setPath(FacetConstants.SOLR_REQUEST_HANDLER_FAST);
        queryRequest.setBasicAuthCredentials(config.getSolrUserReadOnly(), config.getSolrUserReadOnlyPass());
        final QueryResponse response = queryRequest.process(sourceClient);

        final List<String> present = new ArrayList<>(response.getResults().size());
        for (SolrDocument doc : response.getResults()) {
            final Object id = doc.getFieldValue(idField);
            if (id != null) {
                present.add(id.toString());
            }
        }
        return present;
    }

    /**
     * Commits the morgue index.
     *
     * @throws SolrServerException
     * @throws IOException
     */
    public void commit() throws SolrServerException, IOException {
        final UpdateRequest commitRequest = new UpdateRequest();
        commitRequest.setBasicAuthCredentials(config.getSolrUserReadWrite(), config.getSolrUserReadWritePass());
        commitRequest.setAction(UpdateRequest.ACTION.COMMIT, true, true);
        commitRequest.process(morgueClient);
    }

    private String[] fieldsToFetch() {
        final List<String> fields = new ArrayList<>();
        for (FieldKey key : COPIED_KEYS) {
            final String fieldName = fieldNameService.getFieldName(key);
            if (fieldName != null) {
                fields.add(fieldName);
            }
        }
        return fields.toArray(String[]::new);
    }

    private SolrInputDocument toMorgueDocument(SolrDocument source, Date removedDate) {
        final SolrInputDocument target = new SolrInputDocument();
        for (FieldKey key : COPIED_KEYS) {
            copyField(source, target, key);
        }
        final String removedDateField = fieldNameService.getFieldName(FieldKey.REMOVED_DATE);
        if (removedDateField != null) {
            target.setField(removedDateField, removedDate);
        }
        return target;
    }

    private void copyField(SolrDocument source, SolrInputDocument target, FieldKey key) {
        final String field = fieldNameService.getFieldName(key);
        if (field != null && source.containsKey(field)) {
            target.setField(field, source.getFieldValues(field));
        }
    }

    @Override
    public void close() throws IOException {
        morgueClient.close();
    }

}
