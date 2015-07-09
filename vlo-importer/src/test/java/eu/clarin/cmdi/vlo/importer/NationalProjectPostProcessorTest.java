package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class NationalProjectPostProcessorTest extends ImporterTestcase {

    @Test
    public void testNationalProject() {
        NationalProjectPostProcessor processor = new NationalProjectPostProcessor();
        assertEquals("CLARIN-NL", processor.process("Meertens TEST COLLECTION").get(0));
        assertEquals("CLARIN-D", processor.process("Berlin-Brandenburgische Akademie der Wissenschaften").get(0));
        assertEquals("CLARIN-DK-UCPH", processor.process("CLARIN-DK-UCPH Repository").get(0));
        assertEquals("CLARIN-D", processor.process("Universität des Saarlandes CLARIN-D-Zentrum, Saarbrücken").get(0));
    }
}
