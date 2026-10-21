package eu.clarin.cmdi.vlo.service.solr.impl;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.util.NamedList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import org.junit.jupiter.api.Test;

public class AutoCompleteServiceImplTest {

    @Test
    public void primarySuggesterTermsComeBeforeFallback() {
        // Build a minimal Solr suggest response:
        // suggest → { primarySuggester → { "lang" → { suggestions: [...] } }, mySuggester → { ... } }
        // LinkedHashMap preserves insertion order so primary comes before fallback
        Map<String, Object> suggestSection = new LinkedHashMap<>();
        suggestSection.put("primarySuggester", Map.of("lang",
                Map.of("suggestions", List.of(Map.of("term", "language"), Map.of("term", "languages")))
        ));
        suggestSection.put("mySuggester", Map.of("lang",
                Map.of("suggestions", List.of(Map.of("term", "language"), Map.of("term", "language05")))
        ));
        NamedList<Object> response = new NamedList<>();
        response.add("suggest", suggestSection);

        AutoCompleteServiceImpl service = new AutoCompleteServiceImpl(null, new VloConfig(), stubFieldNames()) {
            @Override
            protected NamedList<Object> fireRawQuery(QueryRequest req) {
                return response;
            }
        };

        var actual = Lists.newArrayList(service.getChoices("lang"));
        // "language" deduped, "languages" from primary first, "language05" from fallback after
        assertThat(actual, contains("language", "languages", "language05"));
    }

    private static FieldNameService stubFieldNames() {
        return new FieldNameService() {
            @Override public String getFieldName(FieldKey key) { return "id"; }
            @Override public String getDeprecatedFieldName(FieldKey key) { return "id"; }
        };
    }
}
