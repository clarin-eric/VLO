package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class CountryNamePostProcessorTest {
    
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
