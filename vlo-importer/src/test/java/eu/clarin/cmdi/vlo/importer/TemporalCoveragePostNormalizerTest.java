package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.normalizer.TemporalCoveragePostNormalizer;

public class TemporalCoveragePostNormalizerTest extends ImporterTestcase {
    @Test
    public void testNationalProject() {
        TemporalCoveragePostNormalizer processor = new TemporalCoveragePostNormalizer();
        assertEquals("2012-12-01/", processor.process("2012-12-01/", null).get(0));
        assertEquals("/2012", processor.process("/2012", null).get(0));
        assertEquals("2012-02", processor.process("2012-02", null).get(0));
        assertEquals("1997-07-16/1997-07-17", processor.process("1997-07-16/1997-07-17", null).get(0));
        assertEquals("1994-11-05", processor.process("1994-11-05T08:15:30-05:00", null).get(0));
        assertEquals(0, processor.process("northlimit=-16.4933; southlimit=-16.5617; westlimit=167.419; eastlimit=167.46", null).size());
        assertEquals(0, processor.process("1. November", null).size());
    }
}
