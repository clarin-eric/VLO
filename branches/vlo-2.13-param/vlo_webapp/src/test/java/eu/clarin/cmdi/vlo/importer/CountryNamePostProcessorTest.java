package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class CountryNamePostProcessorTest {

    static VloConfig config;
    // include the full path in the name of the packaged configuration file

    @Before
    public void setUp() {
        // application configuration
        
        String fileName = VloConfig.class.getResource("/VloConfig.xml").getFile();

        // optionally, check for file existence here

        // read the configuration defined in the file

        config = VloConfig.readConfig(fileName);

        // optionally, modify the configuration here

        // apparantly, this does not make the configuration available
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
