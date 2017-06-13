package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Before;

import org.junit.Test;

public class LicenseTypePostProcessorTest extends ImporterTestcase {

    private LicenseTypePostProcessor processor;

    @Before
    public void setUp() {
        processor = new LicenseTypePostProcessor();
    }

    @Test
    public void testLanguageCode() {
        assertMapping("public", "PUB");
        assertMapping("CLARIN-PUB", "PUB");
        assertMapping("academic", "ACA");
        assertMapping("CLARIN-ACA", "ACA");
        assertMapping("restricted", "RES");
        assertMapping("CLARIN-RES", "RES");
    }

    private void assertMapping(String value, String... target) {
        List<String> normalizedVals;
        normalizedVals = processor.process(value, null);
        assertEquals(target.length, normalizedVals.size());
        for (int i = 0; i < target.length; i++) {
            assertEquals(target[i], normalizedVals.get(i));
        }
    }
}
