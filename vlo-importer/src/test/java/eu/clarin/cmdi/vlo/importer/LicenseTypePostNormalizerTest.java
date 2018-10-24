package eu.clarin.cmdi.vlo.importer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Before;

import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.normalizer.LicenseTypePostNormalizer;
import java.util.Collection;
import java.util.Map;

public class LicenseTypePostNormalizerTest extends ImporterTestcase {

    private LicenseTypePostNormalizer processor;

    @Before
    public void setUp() {
        processor = new LicenseTypePostNormalizer(config);
    }

    @Test
    public void testValueMapping() {
        assertMapping("public", null, "PUB");
        assertMapping("CLARIN-PUB", null, "PUB");
        assertMapping("academic", null, "ACA");
        assertMapping("CLARIN-ACA", null, "ACA");
        assertMapping("restricted", null, "RES");
        assertMapping("CLARIN-RES", null, "RES");
    }

    @Test
    public void testMapExplicitValue() {
        final MockDocFieldContainer fieldContainer = new MockDocFieldContainer(
                "record1",
                ImmutableMap.of("availability", ImmutableList.of("ACA", "RES"))
        );

        //availability value should NOT fill in for explicit value
        assertMapping("PUB", fieldContainer, "PUB");
        assertMapping("public", fieldContainer, "PUB");
        assertMapping("ACA", fieldContainer, "ACA");
    }

    @Test
    public void testMapNoExplicitValue() {
        final MockDocFieldContainer fieldContainer = new MockDocFieldContainer(
                "record1",
                ImmutableMap.of("availability", ImmutableList.of("ACA", "RES"))
        );

        //availability value(s) should fill in for explicit value
        assertMapping(null, fieldContainer, "ACA", "RES");
    }

    @Test
    public void testMapNoFallbackValue() {
        final MockDocFieldContainer fieldContainer = new MockDocFieldContainer(
                "record1",
                ImmutableMap.of()
        );

        //no availability value(s) to fill in for explicit value -> no results
        assertMapping(null, fieldContainer);
    }

    private void assertMapping(String value, DocFieldContainer fieldContainer, String... target) {
        List<String> normalizedVals;
        normalizedVals = processor.process(value, fieldContainer);
        assertEquals(target.length, normalizedVals.size());
        for (int i = 0; i < target.length; i++) {
            assertEquals(target[i], normalizedVals.get(i));
        }
    }

    private static class MockDocFieldContainer implements DocFieldContainer {

        private final Map<String, Collection<Object>> fields;

        public MockDocFieldContainer(String id, Map<String, Collection<Object>> fields) {
            this.fields = fields;
        }

        @Override
        public Collection<Object> getDocField(String name) {
            return fields.get(name);
        }

    }
}
