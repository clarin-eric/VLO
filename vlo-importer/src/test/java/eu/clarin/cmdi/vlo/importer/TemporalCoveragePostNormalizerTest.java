package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;

import eu.clarin.cmdi.vlo.importer.normalizer.TemporalCoveragePostNormalizer;
import org.junit.jupiter.api.BeforeEach;

public class TemporalCoveragePostNormalizerTest extends ImporterTestcase {
    private TemporalCoveragePostNormalizer processor;

    @BeforeEach
    public void setUp() {
        processor = new TemporalCoveragePostNormalizer(config);
    }

    @Test
    public void testNationalProject() {
        assertEquals("[2012 TO 2012]", processor.process("2012-12-01/", null).get(0));
        assertEquals("[2012 TO 2012]", processor.process("/2012", null).get(0));
        assertEquals("[2012 TO 2012]", processor.process("2012-02", null).get(0));
        assertEquals("[1996 TO 1997]", processor.process("1996-07-16/1997-07-17", null).get(0));
        assertEquals("[1994 TO 1994]", processor.process("1994-11-05T08:15:30-05:00", null).get(0));
        assertEquals(0, processor.process("northlimit=-16.4933; southlimit=-16.5617; westlimit=167.419; eastlimit=167.46", null).size());
        assertEquals(0, processor.process("1. November", null).size());
    }
}
