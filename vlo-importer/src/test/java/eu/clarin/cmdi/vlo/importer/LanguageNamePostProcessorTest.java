package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class LanguageNamePostProcessorTest extends ImporterTestcase {

    @Before
    public void setUp() throws Exception {

        // read the configuration from the packaged configuration file
        new DefaultVloConfigFactory().newConfig();

        // optionally, modify the configuration here
    }

    @Test
    public void testLanguageCode() {
        PostProcessor processor = new LanguageNamePostProcessor();
        assertEquals("Dutch", processor.process("NL"));
        assertEquals("English", processor.process("en"));
        assertEquals("Dutch", processor.process("nl"));
        assertEquals("Western Frisian", processor.process("fry"));
        assertEquals("test", processor.process("test"));
        assertEquals("", processor.process(""));
        assertEquals(null, processor.process(null));
        assertEquals("French", processor.process("ISO639-3:fra"));
        assertEquals("German", processor.process("RFC1766:x-sil-GER"));
        assertEquals("RFC1766:sgn-NL", processor.process("RFC1766:sgn-NL"));
        assertEquals("Basque", processor.process("baq"));
    }

}
