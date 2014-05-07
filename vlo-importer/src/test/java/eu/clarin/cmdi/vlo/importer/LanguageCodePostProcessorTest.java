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
        assertEquals("nld", processor.process("NL"));
        assertEquals("eng", processor.process("en"));
        assertEquals("nld", processor.process("nl"));
        assertEquals("fry", processor.process("fry"));
        assertEquals("test", processor.process("test"));
        assertEquals("", processor.process(""));
        assertEquals(null, processor.process(null));
        assertEquals("fra", processor.process("ISO639-3:fra"));
        assertEquals("deu", processor.process("RFC1766:x-sil-GER"));
        assertEquals("RFC1766:sgn-NL", processor.process("RFC1766:sgn-NL"));
        assertEquals("eus", processor.process("baq"));
        assertEquals("eng", processor.process("eng"));
        assertEquals("eng", processor.process("English"));
        assertEquals("deu", processor.process("German"));
        assertEquals("esn", processor.process("Salvadoran Sign Language"));
    }

}
