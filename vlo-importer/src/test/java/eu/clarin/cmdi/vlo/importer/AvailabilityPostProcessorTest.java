package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class AvailabilityPostProcessorTest extends ImporterTestcase {
    

    @Test
    public void testLanguageCode() {
        PostProcessor processor = new AvailabilityPostProcessor();    
        List<String> normalizedVals;
        
        //"Apache Licence 2.0" -> "PUB;BY"
        normalizedVals = processor.process("Apache Licence 2.0");
        assertEquals(2, normalizedVals.size());
        assertEquals("PUB", normalizedVals.get(0));
        assertEquals("BY", normalizedVals.get(1));
        assertEquals("PUB", processor.process("Open Access").get(0));
        assertEquals("PUB", processor.process("open access").get(0));
        assertEquals("PUB", processor.process("Open Access").get(0));
        assertEquals("ACA", processor.process("free; Free for academic use.").get(0));
        assertEquals("RES", processor.process("Please contact contact-person").get(0));
     
        //"GNU General Public License, version 3" -> "PUB;BY;SA"
        normalizedVals = processor.process("GNU General Public License, version 3");
        assertEquals(3, normalizedVals.size());
        assertEquals("PUB", normalizedVals.get(0));
        assertEquals("BY", normalizedVals.get(1));
        assertEquals("SA", normalizedVals.get(2));
    }
}
