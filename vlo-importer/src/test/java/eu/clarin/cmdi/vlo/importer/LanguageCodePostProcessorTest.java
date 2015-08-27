package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class LanguageCodePostProcessorTest extends ImporterTestcase {

    @Before
    public void setUp() throws Exception {

        // read the configuration from the packaged configuration file
        new DefaultVloConfigFactory().newConfig();

        // optionally, modify the configuration here
    }

    @Test
    public void testLanguageCode() {
        PostProcessor processor = new LanguageCodePostProcessor();
        assertEquals("code:nld", processor.process("NL").get(0));
        assertEquals("code:eng", processor.process("en").get(0));
        assertEquals("code:fry", processor.process("fry").get(0));
        assertEquals("name:test", processor.process("test").get(0));
        assertEquals("", processor.process("").get(0));
        assertEquals(null, processor.process(null).get(0));
        assertEquals("code:fra", processor.process("ISO639-3:fra").get(0));
        assertEquals("code:deu", processor.process("RFC1766:x-sil-GER").get(0));
        assertEquals("name:RFC1766:sgn-NL", processor.process("RFC1766:sgn-NL").get(0));
        assertEquals("code:eus", processor.process("baq").get(0));
        assertEquals("code:eng", processor.process("eng").get(0));
        assertEquals("code:eng", processor.process("English").get(0));
        assertEquals("code:esn", processor.process("Salvadoran Sign Language").get(0));
        assertEquals("code:eng", processor.process("en_US").get(0));
        assertEquals("code:nld", processor.process("nl-NL").get(0));
	assertEquals("code:eng", processor.process("ISO639-2:eng").get(0));
        assertEquals("code:spa", processor.process("Spanish, Castilian").get(0));
        assertEquals("code:ron", processor.process("Romanian").get(0));
    }
}
