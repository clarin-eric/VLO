package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloConfigFactory;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class ImporterTestcase {

    private final VloConfigFactory configFactory = new DefaultVloConfigFactory();
    protected VloConfig config;
    protected LanguageCodeUtils languageCodeUtils;
    private char ch = 'a';

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected File createCmdiFile(String name, String content) throws IOException {
        File file = tempFolder.newFile(name + System.currentTimeMillis() + "_" + ch++ + ".cmdi");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }

    @Before
    public void setup() throws Exception {
        // read the configuration defined in the packaged configuration file
        // and configure to use bundled mappings
        config = DefaultVloConfigFactory.configureDefaultMappingLocations(configFactory.newConfig());
        languageCodeUtils = new LanguageCodeUtils(config);
    }

    public static String getTestFacetConceptFilePath() {
        try {
            return new File(ImporterTestcase.class.getResource("/facetConceptsTest.xml").toURI()).getAbsolutePath();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

}
