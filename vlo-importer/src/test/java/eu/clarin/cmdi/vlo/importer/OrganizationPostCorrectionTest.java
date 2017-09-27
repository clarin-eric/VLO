package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.clarin.cmdi.vlo.importer.processor.OrganisationPostProcessor;
import eu.clarin.cmdi.vlo.importer.processor.PostProcessor;

public class OrganizationPostProcessorTest extends ImporterTestcase {


    @Test
    public void testLanguageCode() {
        PostProcessor processor = new OrganisationPostProcessor(config);
        assertEquals("Department of Psychology, Ohio State University", processor.process("http://buckeyecorpus.osu.edu", null).get(0));
        assertEquals("s.n.", processor.process("s.n", null).get(0));
        assertEquals("SELAF", processor.process("Société des Etudes Linguistiques et Anthropologiques de France", null).get(0));
        assertEquals("NIAS", processor.process("Netherlands Institute of Advanced Study", null).get(0));
    }
}
