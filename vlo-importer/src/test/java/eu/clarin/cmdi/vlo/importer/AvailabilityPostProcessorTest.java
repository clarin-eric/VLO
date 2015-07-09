package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AvailabilityPostProcessorTest extends ImporterTestcase {
    

    @Test
    public void testLanguageCode() {
        PostProcessor processor = new AvailabilityPostProcessor();     
        assertEquals("Free", processor.process("Apache Licence 2.0").get(0));
        assertEquals("Free", processor.process("Open Access").get(0));
        assertEquals("Free", processor.process("open access").get(0));
        assertEquals("Free", processor.process("Open Access").get(0));
        assertEquals("Free", processor.process("free; Free for academic use.").get(0));
        assertEquals("Free for academic use", processor.process("CC BY-NC 3.0 DE").get(0));
        assertEquals("Upon request", processor.process("Please contact contact-person").get(0));
        
        
    }
}
