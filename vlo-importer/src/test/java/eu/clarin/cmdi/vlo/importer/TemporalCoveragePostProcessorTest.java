package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class TemporalCoveragePostProcessorTest extends ImporterTestcase {
    @Test
    public void testNationalProject() {
        TemporalCoveragePostProcessor processor = new TemporalCoveragePostProcessor();
        assertEquals("2012-12-01/", processor.process("2012-12-01/").get(0));
        assertEquals("/2012", processor.process("/2012").get(0));
        assertEquals("2012-02", processor.process("2012-02").get(0));
        assertEquals(0, processor.process("1. November").size());
    }
}
