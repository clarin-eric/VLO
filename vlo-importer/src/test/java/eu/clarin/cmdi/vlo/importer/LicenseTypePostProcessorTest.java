package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class LicenseTypePostProcessorTest extends ImporterTestcase {
    

    @Test
    public void testLanguageCode() {
        PostProcessor processor = new LicenseTypePostProcessor();    
        List<String> normalizedVals;
        
        normalizedVals = processor.process("public");
        assertEquals(1, normalizedVals.size());
        assertEquals("PUB", normalizedVals.get(0));
     
        normalizedVals = processor.process("academic");
        assertEquals(1, normalizedVals.size());
        assertEquals("ACA", normalizedVals.get(0));
    }
}
