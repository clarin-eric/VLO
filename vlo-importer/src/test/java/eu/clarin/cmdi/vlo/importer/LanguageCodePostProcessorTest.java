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
        assertEquals("code:nld", processor.process("NL"));
        assertEquals("code:eng", processor.process("en"));
        assertEquals("code:nld", processor.process("nl"));
        assertEquals("code:fry", processor.process("fry"));
        assertEquals("name:test", processor.process("test"));
        assertEquals("", processor.process(""));
        assertEquals(null, processor.process(null));
        assertEquals("code:fra", processor.process("ISO639-3:fra"));
        assertEquals("code:deu", processor.process("RFC1766:x-sil-GER"));
        assertEquals("name:RFC1766:sgn-NL", processor.process("RFC1766:sgn-NL"));
        assertEquals("code:eus", processor.process("baq"));
        assertEquals("code:eng", processor.process("eng"));
        assertEquals("code:eng", processor.process("English"));
        assertEquals("code:deu", processor.process("German"));
        assertEquals("code:esn", processor.process("Salvadoran Sign Language"));
    }

}
