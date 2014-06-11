package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class LanguageLinkPostProcessorTest extends ImporterTestcase {

    @Before
    public void setUp() throws Exception {

        // read the configuration from the packaged configuration file
        new DefaultVloConfigFactory().newConfig();

        // optionally, modify the configuration here
    }

    @Test
    public void testLanguageLink() {
        PostProcessor processor = new LanguageLinkPostProcessor();
        assertEquals("<a href=\"http://infra.clarin.eu/service/language/info.php?code=nld\">Dutch</a>", processor.process("nld").get(0));
    }
}
