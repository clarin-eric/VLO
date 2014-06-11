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
        assertEquals("Dutch", processor.process("NL").get(0));
        assertEquals("English", processor.process("en").get(0));
        assertEquals("Dutch", processor.process("nl").get(0));
        assertEquals("Western Frisian", processor.process("fry").get(0));
        assertEquals("test", processor.process("test").get(0));
        assertEquals("", processor.process("").get(0));
        assertEquals(null, processor.process(null).get(0));
        assertEquals("French", processor.process("ISO639-3:fra").get(0));
        assertEquals("German", processor.process("RFC1766:x-sil-GER").get(0));
        assertEquals("RFC1766:sgn-NL", processor.process("RFC1766:sgn-NL").get(0));
        assertEquals("Basque", processor.process("baq").get(0));
    }

}
