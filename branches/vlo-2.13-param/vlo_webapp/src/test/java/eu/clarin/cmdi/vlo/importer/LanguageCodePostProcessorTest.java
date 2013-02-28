package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class LanguageCodePostProcessorTest {
 
    // application configuration
    static VloConfig config;

    @Before
    public void setUp() {
        
        // include the full path in the name of the packaged configuration file
        String fileName = VloConfig.class.getResource("/VloConfig.xml").getFile();

        // read the configuration defined in the file
        config = VloConfig.readTestConfig(fileName);

        // optionally, modify the configuration here
    }

    @Test
    public void testLanguageCode() {
        LanguageCodePostProcessor processor = new LanguageCodePostProcessor();
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
    }

}
