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
package eu.clarin.cmdi.vlo.solr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonCollectors;
import javax.json.stream.JsonGenerator;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;

/**
 * Runnable utility class that creates a JSON file with representations of all
 * documents resulting from a query on a running Solr instance
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class SolrInputDataCreator {

    /**
     * Solr instance to query
     */
    private final static String SOLR_URL = "http://localhost:8983/solr/vlo-index";

    /**
     * Parameters that define the query to be carried out
     */
    private final static Map<String, String> QUERY_PARAMS
            = ImmutableMap.of(
                    "q", "*:*",
                    "rows", "1000"
            );

    /**
     * Solr fields to include in the JSON output
     */
    private final static List<String> INCLUDED_FIELDS = ImmutableList.of(
            "id", "name", "_languageName", "collection", "country", "genre", "resourceClass", "_resourceRefCount"
    );

    /**
     * Options for JSON writer (see {@link JsonWriterFactory})
     */
    private final static Map<String, Object> JSON_WRITER_PROPS = ImmutableMap.of(
            JsonGenerator.PRETTY_PRINTING, true
    );

    private void writeJson(Writer writer) throws Exception {
        try (SolrClient client = newSolrClient()) {
            writeResults(writer, getResults(client));
        }
    }

    private SolrDocumentList getResults(final SolrClient client) throws IOException, SolrServerException {
        return client
                .query(new MapSolrParams(QUERY_PARAMS))
                .getResults();
    }

    private void writeResults(Writer writer, SolrDocumentList results) throws IOException {
        final Iterator<SolrDocument> resultsIterator = results.iterator();
        final Iterable<SolrDocument> iterable = () -> resultsIterator;
        final Stream<SolrDocument> resultStream = StreamSupport.stream(iterable.spliterator(), false);

        final JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(JSON_WRITER_PROPS);
        try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(writer)) {
            final JsonArray resultsArray
                    = resultStream
                            //map to JSON
                            .map(SolrInputDataCreator::solrDocToJson)
                            //collect into array
                            .collect(JsonCollectors.toJsonArray());
            jsonWriter.write(resultsArray);
        }
    }

    private static JsonObject solrDocToJson(SolrDocument doc) {
        final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        doc.getFieldNames().stream()
                .filter((field) -> (INCLUDED_FIELDS.contains(field)))
                .forEach((field) -> {
                    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                    doc.getFieldValues(field).stream().forEach((value) -> {
                        arrayBuilder.add(value.toString());
                    });
                    objectBuilder.add(field, arrayBuilder.build());

                });
        return objectBuilder.build();
    }

    private static SolrInputDocument jsonToSolrInputDocument(JsonValue t) {
        final SolrInputDocument solrInputDocument = new SolrInputDocument();
        final JsonObject documentJson = t.asJsonObject();
        documentJson.keySet().forEach((String key) -> {
            documentJson.getJsonArray(key).stream().forEach((JsonValue v) -> {
                if (v.getValueType() == JsonValue.ValueType.STRING) {
                    solrInputDocument.addField(key, ((JsonString) v).getString());
                } else {
                    solrInputDocument.addField(key, v.toString());
                }
            });
        });
        return solrInputDocument;

    }

    private static HttpSolrClient newSolrClient() {
        return new HttpSolrClient.Builder()
                .withBaseSolrUrl(SOLR_URL)
                .build();
    }

    /**
     * Reads documents from a JSON structure created with this utility
     *
     * @param inputStream
     * @return
     */
    public static final List<SolrInputDocument> getDocumentsFromJson(final InputStream inputStream) {
        try (final JsonReader reader = Json.createReader(inputStream)) {
            return reader
                    .readArray()
                    .stream()
                    .map(SolrInputDataCreator::jsonToSolrInputDocument)
                    .collect(Collectors.toList());
        }
    }

    public static final void main(String[] args) throws Exception {
        final File outFile = File.createTempFile("solr", ".json");

        try (Writer writer = new FileWriter(outFile)) {
            new SolrInputDataCreator().writeJson(writer);
        }

        System.out.println("Written result to " + outFile.getAbsolutePath());
    }

}
