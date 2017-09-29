package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.normalizer.AbstractPostNormalizer;
import eu.clarin.cmdi.vlo.importer.normalizer.AvailabilityPostNormalizer;


public class AvailabilityPostNormalizerTest extends ImporterTestcase {
    

    @Test
    public void testLanguageCode() {
        AbstractPostNormalizer processor = new AvailabilityPostNormalizer(config);    
        List<String> normalizedVals;
        
        //"Apache Licence 2.0" -> "PUB;BY"
        normalizedVals = processor.process("Apache Licence 2.0", null);
        assertEquals(2, normalizedVals.size());
        assertEquals("PUB", normalizedVals.get(0));
        assertEquals("BY", normalizedVals.get(1));
        assertEquals("PUB", processor.process("Open Access", null).get(0));
        assertEquals("PUB", processor.process("open access", null).get(0));
        assertEquals("PUB", processor.process("Open Access", null).get(0));
        assertEquals("ACA", processor.process("free; Free for academic use.", null).get(0));
        assertEquals("RES", processor.process("Please contact contact-person", null).get(0));
     
        //"GNU General Public License, version 3" -> "PUB;BY;SA"
        normalizedVals = processor.process("GNU General Public License, version 3", null);
        assertEquals(3, normalizedVals.size());
        assertEquals("PUB", normalizedVals.get(0));
        assertEquals("BY", normalizedVals.get(1));
        assertEquals("SA", normalizedVals.get(2));
    }
}
