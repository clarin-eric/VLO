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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CursorMarkParams;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MorgueIndexer} using fake (in-memory) SolrClient stubs
 */
public class MorgueIndexerTest extends ImporterTestcase {

    private FieldNameService fieldNameService;
    private CapturingMorgueSolrClient morgueClient;
    private MorgueIndexer indexer;

    @BeforeEach
    public void setUp() throws Exception {
        config.setSolrMorgueUrl("http://localhost:fake/solr/vlo-morgue/");
        fieldNameService = new FieldNameServiceImpl(config);
    }

    // Simple happy path test - archived data fields must match
    @Test
    public void archivesFieldsFromSourceDocument() throws Exception {
        final Instant removedAt = Instant.parse("2026-03-01T12:00:00Z");

        final SolrDocument sourceDoc = new SolrDocument();
        sourceDoc.addField(fieldNameService.getFieldName(FieldKey.ID), "record-1");
        sourceDoc.addField(fieldNameService.getFieldName(FieldKey.NAME), "My record");
        sourceDoc.addField(fieldNameService.getFieldName(FieldKey.DESCRIPTION), "A description");
        sourceDoc.addField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER), "Provider A");
        sourceDoc.addField(fieldNameService.getFieldName(FieldKey.FIRST_SEEN), Date.from(Instant.parse("2024-01-01T00:00:00Z")));
        sourceDoc.addField(fieldNameService.getFieldName(FieldKey.LAST_SEEN), Date.from(Instant.parse("2026-02-01T00:00:00Z")));
        sourceDoc.addField("someOtherField", "should not be copied");

        morgueClient = new CapturingMorgueSolrClient();
        indexer = new MorgueIndexer(config, fieldNameService,
                FakeSourceSolrClient.singlePage(List.of(sourceDoc)), morgueClient);

        final int archived = indexer.archiveRemovedRecords("*:*", removedAt);

        Assertions.assertEquals(1, archived, "one record should be archived");

        final List<SolrInputDocument> written = morgueClient.getIndexedDocuments();
        Assertions.assertEquals(1, written.size());

        final SolrInputDocument morgueDoc = written.getFirst();

        // copied fields
        Assertions.assertEquals("record-1", morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.ID)));
        Assertions.assertEquals("My record", morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.NAME)));
        Assertions.assertEquals("Provider A", morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER)));
        Assertions.assertNotNull(morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.FIRST_SEEN)));
        Assertions.assertNotNull(morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.LAST_SEEN)));

        // removal timestamp must be set
        Assertions.assertEquals(Date.from(removedAt), morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.REMOVED_AT)));

        // fields not in the copy list must be absent (description is intentionally not archived)
        Assertions.assertNull(morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.DESCRIPTION)));
        Assertions.assertNull(morgueDoc.getFieldValue("someOtherField"));
    }

    @Test
    public void archivesMultipleDocumentsAndReturnsCount() throws Exception {
        final List<SolrDocument> sourceDocs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final SolrDocument doc = new SolrDocument();
            doc.addField(fieldNameService.getFieldName(FieldKey.ID), "record-" + i);
            doc.addField(fieldNameService.getFieldName(FieldKey.DATA_PROVIDER), "Provider");
            sourceDocs.add(doc);
        }

        morgueClient = new CapturingMorgueSolrClient();
        indexer = new MorgueIndexer(config, fieldNameService,
                FakeSourceSolrClient.singlePage(sourceDocs), morgueClient);

        final int archived = indexer.archiveRemovedRecords("*:*", Instant.now());

        Assertions.assertEquals(3, archived);
        Assertions.assertEquals(3, morgueClient.getIndexedDocuments().size());
    }

    @Test
    public void returnsZeroWhenSourceIsEmpty() throws Exception {
        morgueClient = new CapturingMorgueSolrClient();
        indexer = new MorgueIndexer(config, fieldNameService,
                FakeSourceSolrClient.singlePage(List.of()), morgueClient);

        final int archived = indexer.archiveRemovedRecords("*:*", Instant.now());

        Assertions.assertEquals(0, archived);
        Assertions.assertEquals(0, morgueClient.getIndexedDocuments().size());
    }

    @Test
    public void handlesOptionalFieldsGracefully() throws Exception {
        // Document with only the mandatory ID field — optional fields absent
        final SolrDocument minimalDoc = new SolrDocument();
        minimalDoc.addField(fieldNameService.getFieldName(FieldKey.ID), "minimal-record");

        morgueClient = new CapturingMorgueSolrClient();
        indexer = new MorgueIndexer(config, fieldNameService,
                FakeSourceSolrClient.singlePage(List.of(minimalDoc)), morgueClient);

        final int archived = indexer.archiveRemovedRecords("*:*", Instant.now());

        Assertions.assertEquals(1, archived);
        final SolrInputDocument morgueDoc = morgueClient.getIndexedDocuments().getFirst();
        Assertions.assertEquals("minimal-record", morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.ID)));
        // optional fields should simply be absent, not throw
        Assertions.assertNull(morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.NAME)));
        Assertions.assertNull(morgueDoc.getFieldValue(fieldNameService.getFieldName(FieldKey.DESCRIPTION)));
    }

    @Test
    public void archivesAcrossMultipleCursorPages() throws Exception {
        final List<SolrDocument> sourceDocs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final SolrDocument doc = new SolrDocument();
            doc.addField(fieldNameService.getFieldName(FieldKey.ID), "record-" + i);
            sourceDocs.add(doc);
        }

        morgueClient = new CapturingMorgueSolrClient();
        // page size 2 → 5 records arrive as pages of 2 + 2 + 1
        final FakeSourceSolrClient source = new FakeSourceSolrClient(sourceDocs, 2);
        indexer = new MorgueIndexer(config, fieldNameService, source, morgueClient);

        final int archived = indexer.archiveRemovedRecords("*:*", Instant.now());

        Assertions.assertEquals(5, archived, "all records across pages should be archived");
        Assertions.assertEquals(5, morgueClient.getIndexedDocuments().size());
        // three pages carrying data plus one terminating (empty) page whose
        // cursor mark no longer advances
        Assertions.assertEquals(4, source.getRequestCount(),
                "should keep paging until the cursor mark stops advancing");
    }

    @Test
    public void commitSendsCommitRequest() throws Exception {
        morgueClient = new CapturingMorgueSolrClient();
        indexer = new MorgueIndexer(config, fieldNameService,
                FakeSourceSolrClient.singlePage(List.of()), morgueClient);

        indexer.commit();

        Assertions.assertTrue(morgueClient.wasCommitted(), "commit() should send a commit request to the morgue");
    }

    /**
     * Source (read) fake that serves the documents in fixed-size pages, driven by
     * a cursor mark. When there are no more documents it echoes back the incoming
     * cursor mark, which is how a real Solr signals the end of a cursor scan.
     *
     * <p>
     * Use {@link #singlePage(List)} when paging is irrelevant to the test — all
     * documents are then returned in a single page.</p>
     */
    private static class FakeSourceSolrClient extends SolrClient {

        private final List<SolrDocument> allDocs;
        private final int pageSize;
        private int served = 0;
        private int requestCount = 0;

        /**
         * @return a client that returns all documents in one page (no real paging)
         */
        static FakeSourceSolrClient singlePage(List<SolrDocument> docs) {
            return new FakeSourceSolrClient(docs, Integer.MAX_VALUE);
        }

        FakeSourceSolrClient(List<SolrDocument> allDocs, int pageSize) {
            this.allDocs = allDocs;
            this.pageSize = pageSize;
        }

        int getRequestCount() {
            return requestCount;
        }

        @Override
        public NamedList<Object> request(SolrRequest<?> request, String collection) {
            requestCount++;
            final String incomingCursor = request.getParams().get(CursorMarkParams.CURSOR_MARK_PARAM);

            final NamedList<Object> response = new NamedList<>();
            final SolrDocumentList docList = new SolrDocumentList();
            docList.setNumFound(allDocs.size());

            final int from = served;
            final int to = Math.min(from + pageSize, allDocs.size());
            for (int i = from; i < to; i++) {
                docList.add(allDocs.get(i));
            }
            response.add("response", docList);

            if (from >= allDocs.size()) {
                // exhausted: echo the incoming mark so the loop terminates
                response.add(CursorMarkParams.CURSOR_MARK_NEXT, incomingCursor);
            } else {
                served = to;
                response.add(CursorMarkParams.CURSOR_MARK_NEXT, "cursor-" + served);
            }
            return response;
        }

        @Override
        public void close() {
        }
    }

    /**
     * Morgue (write) stub: captures every document submitted via UpdateRequest
     * and records whether a commit was requested.
     */
    private static class CapturingMorgueSolrClient extends SolrClient {

        private final List<SolrInputDocument> indexed = new ArrayList<>();
        private boolean committed = false;

        List<SolrInputDocument> getIndexedDocuments() {
            return indexed;
        }

        boolean wasCommitted() {
            return committed;
        }

        @Override
        public NamedList<Object> request(SolrRequest<?> request, String collection)
                throws SolrServerException, IOException {
            if (request instanceof UpdateRequest update) {
                if (update.getAction() == UpdateRequest.ACTION.COMMIT) {
                    committed = true;
                }
                final Collection<SolrInputDocument> docs = update.getDocuments();
                if (docs != null) {
                    indexed.addAll(docs);
                }
            }
            return new NamedList<>();
        }

        @Override
        public void close() {
        }
    }
}
