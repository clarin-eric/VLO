package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.LanguageCodePostNormalizer;


import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class LanguageCodePostNormalizerTest extends ImporterTestcase {

    @Before
    public void setUp() throws Exception {

        // read the configuration from the packaged configuration file
        new DefaultVloConfigFactory().newConfig();

        // optionally, modify the configuration here
    }

    @Test
    public void testLanguageCode() {
        AbstractPostNormalizer processor = new LanguageCodePostNormalizer(config, languageCodeUtils);
        assertEquals("code:nld", processor.process("NL", null).get(0));
        assertEquals("code:eng", processor.process("en", null).get(0));
        assertEquals("code:fry", processor.process("fry", null).get(0));
        assertEquals("name:Test", processor.process("test", null).get(0));
        assertEquals("", processor.process("", null).get(0));
        assertEquals(null, processor.process(null, null).get(0));
        assertEquals("code:fra", processor.process("ISO639-3:fra", null).get(0));
        assertEquals("code:deu", processor.process("RFC1766:x-sil-GER", null).get(0));
        assertEquals("name:RFC1766:sgn-NL", processor.process("RFC1766:sgn-NL", null).get(0));
        assertEquals("code:eus", processor.process("baq", null).get(0));
        assertEquals("code:eng", processor.process("eng", null).get(0));
        assertEquals("code:eng", processor.process("English", null).get(0));
        assertEquals("code:esn", processor.process("Salvadoran Sign Language", null).get(0));
        assertEquals("code:eng", processor.process("en_US", null).get(0));
        assertEquals("code:nld", processor.process("nl-NL", null).get(0));
	assertEquals("code:eng", processor.process("ISO639-2:eng", null).get(0));
        assertEquals("code:spa", processor.process("Spanish, Castilian", null).get(0));
        assertEquals("code:ron", processor.process("Romanian", null).get(0));
    }
}
