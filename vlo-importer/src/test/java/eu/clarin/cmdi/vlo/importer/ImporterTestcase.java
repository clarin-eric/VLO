package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
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
    protected FieldNameService fieldNameService;
    protected LanguageCodeUtils languageCodeUtils;
    protected VLOMarshaller marshaller;
    private char ch = 'a';

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected File createCmdiFile(String name, String content) throws IOException {
        File file = tempFolder.newFile(name + System.currentTimeMillis() + "_" + ch++ + ".cmdi");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }
    
    protected String createTmpFile(String content) throws IOException{
    	File file = tempFolder.newFile(System.currentTimeMillis() + ".tmp");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file.getAbsolutePath();
    }
    
    protected File createValueMappingsFile(String name, String content) throws IOException {
        File file = tempFolder.newFile(name + System.currentTimeMillis() + "_" + ch++ + ".xml");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }

    @Before
    public void setup() throws Exception {
        if (Thread.currentThread().getName().equals("main")) {
            Thread.currentThread().setName("test-main");
        }

        // read the configuration defined in the packaged configuration file
        // and configure to use bundled mappings
        config = DefaultVloConfigFactory.configureDefaultMappingLocations(configFactory.newConfig());
        config.setValueMappingsFile(getTestValueMappingsFilePath());
        fieldNameService = new FieldNameServiceImpl(config);
        languageCodeUtils = new LanguageCodeUtils(config);
        marshaller = new VLOMarshaller();
    }

    public static String getTestFacetConceptFilePath() {
        try {
            return new File(ImporterTestcase.class.getResource("/facetConceptsTest.xml").toURI()).getAbsolutePath();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static String getTestValueMappingsFilePath() {
        try {
            return new File(ImporterTestcase.class.getResource("/valueMappingsTest.xml").toURI()).getAbsolutePath();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

}
