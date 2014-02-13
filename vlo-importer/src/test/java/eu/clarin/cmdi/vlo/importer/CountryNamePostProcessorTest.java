package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class CountryNamePostProcessorTest {

    @Before
    public void setUp() {
        
        // read the configuration from the packaged configuration file
        new DefaultVloConfigFactory().newConfig();

        // optionally, modify the configuration here
    }
    
    @Test
    public void testCountryCode() {
        CountryNamePostProcessor processor = new CountryNamePostProcessor();
        assertEquals("Netherlands", processor.process("NL"));
        assertEquals("United Kingdom", processor.process("GB"));
        assertEquals("Netherlands", processor.process("nl"));
        assertEquals("test", processor.process("test"));
        assertEquals("", processor.process(""));
        assertEquals(null, processor.process(null));
    }
}
