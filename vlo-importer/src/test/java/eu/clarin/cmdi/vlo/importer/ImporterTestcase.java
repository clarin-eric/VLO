package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloConfigFactory;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class ImporterTestcase {

    private final VloConfigFactory configFactory = new DefaultVloConfigFactory();
    protected VloConfig config;
    private char ch = 'a';

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected File createCmdiFile(String name, String content) throws IOException {
        File file = tempFolder.newFile(name + System.currentTimeMillis() + "_" + ch++ + ".cmdi");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }

    @After
    public void cleanup() {
        MetadataImporter.config = null;
    }

    @Before
    public void setup() throws Exception {
        // read the configuration defined in the packaged configuration file
        MetadataImporter.config = configFactory.newConfig();

        // optionally, modify the configuration here
        MetadataImporter.config.setComponentRegistryRESTURL("http://alpha-vlo.clarin.eu/component-registry-rest/rest/registry/1.x/profiles/");
        config = MetadataImporter.config;

        MetadataImporter.languageCodeUtils = new LanguageCodeUtils(config);
    }

    public static String getTestFacetConceptFilePath() {
        try {
            return new File(ImporterTestcase.class.getResource("/facetConceptsTest.xml").toURI()).getAbsolutePath();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

}
