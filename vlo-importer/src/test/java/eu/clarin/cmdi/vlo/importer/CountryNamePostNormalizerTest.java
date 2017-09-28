package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.normalizer.CountryNamePostNormalizer;

public class CountryNamePostNormalizerTest extends ImporterTestcase {

    @Test
    public void testCountryCode() {
        CountryNamePostNormalizer processor = new CountryNamePostNormalizer(config);
        assertEquals("Netherlands", processor.process("NL", null).get(0));
        assertEquals("United Kingdom", processor.process("GB", null).get(0));
        assertEquals("Netherlands", processor.process("nl", null).get(0));
        assertEquals("test", processor.process("test", null).get(0));
        assertEquals("", processor.process("", null).get(0));
        assertEquals(null, processor.process(null, null).get(0));
    }
}
