package eu.clarin.cmdi.vlo.importer;

import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.CreatorPostNormalizer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class CreatorPostNormalizerTest extends ImporterTestcase {

    @Test
    public void testCreatorValues() {
        AbstractPostNormalizer processor = new CreatorPostNormalizer();

        // value extraction of format: "ROLE: PERSON"
        assertEquals("John Doe", processor.process("Author: John Doe", null).get(0));

        // URLs -> discarded as long as authority file URLs aren't resolved
        assertNull(processor.process("http://orcid.org/0000-0000-0000-TEST", null).get(0));
    }
}
