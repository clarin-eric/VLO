package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AvailabilityPostProcessorTest extends ImporterTestcase {
    

    @Test
    public void testAvailabilityMapping() {
        PostProcessor processor = new AvailabilityPostProcessor();     
        assertEquals("PUB;BY", processor.process("Apache Licence 2.0").get(0));
        assertEquals("PUB", processor.process("Open Access").get(0));
        assertEquals("PUB", processor.process("open access").get(0));
        assertEquals("PUB", processor.process("Open Access").get(0));
        assertEquals("ACA", processor.process("free; Free for academic use.").get(0));
        assertEquals("PUB;BY;NC", processor.process("CC BY-NC 3.0 DE").get(0));
        assertEquals("RES", processor.process("Please contact contact-person").get(0));
        
        
    }
}
