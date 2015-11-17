package eu.clarin.cmdi.vlo.importer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrganizationPostProcessorTest extends ImporterTestcase {


    @Test
    public void testLanguageCode() {
        PostProcessor processor = new OrganisationPostProcessor();
        assertEquals("Department of Psychology, Ohio State University", processor.process("http://buckeyecorpus.osu.edu").get(0));
        assertEquals("s.n.", processor.process("s.n").get(0));
        assertEquals("SELAF", processor.process("Société des Etudes Linguistiques et Anthropologiques de France").get(0));
        assertEquals("NIAS", processor.process("Netherlands Institute of Advanced Study").get(0));
    }
}
