package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.importer.normalizer.NationalProjectPostNormalizer;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class NationalProjectPostNormalizerTest extends ImporterTestcase {

    @Test
    public void testNationalProject() {
        NationalProjectPostNormalizer processor = new NationalProjectPostNormalizer(config);
        assertEquals("CLARIN-NL", processor.process("Meertens TEST COLLECTION", null).get(0));
        assertEquals("CLARIN-DK-UCPH", processor.process("CLARIN-DK-UCPH Repository", null).get(0));
        assertEquals("CLARIN-D", processor.process("Universität des Saarlandes CLARIN-D-Zentrum, Saarbrücken", null).get(0));
    }
}
