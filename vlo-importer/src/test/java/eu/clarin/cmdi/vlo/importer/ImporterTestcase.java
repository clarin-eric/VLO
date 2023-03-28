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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class ImporterTestcase {

    private final static VloConfigFactory configFactory = new DefaultVloConfigFactory();
    protected static VloConfig config;
    protected static LanguageCodeUtils languageCodeUtils;

    protected FieldNameService fieldNameService;
    protected VLOMarshaller marshaller;
    private char ch = 'a';

    @TempDir
    public File tempFolder;

    protected File createCmdiFile(String name, String content) throws IOException {

        File file = new File(tempFolder, name + System.currentTimeMillis() + "_" + ch++ + ".cmdi");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }

    protected String createTmpFile(String content) throws IOException {
        File file = new File(tempFolder, System.currentTimeMillis() + ".tmp");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file.getAbsolutePath();
    }

    protected File createValueMappingsFile(String name, String content) throws IOException {
        File file = new File(tempFolder, name + System.currentTimeMillis() + "_" + ch++ + ".xml");
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }

    @BeforeAll
    public static void setupClass() throws Exception {
        // read the configuration defined in the packaged configuration file
        // and configure to use bundled mappings
        config = DefaultVloConfigFactory.configureDefaultMappingLocations(configFactory.newConfig());
        config.setValueMappingsFile(getTestValueMappingsFilePath());
        config.setSilToISO639CodesUrl(getSilToIsoMapUrl());
        config.setCountryComponentUrl(getCountryCodeComponentUrl());
        config.setLanguage2LetterCodeComponentUrl(getLanguage2LetterCodeUrl());
        config.setLanguage3LetterCodeComponentUrl(getLanguage3LetterCodeUrl());
        config.setLinkCheckerDbConnectionString(null);
        config.setLinkCheckerDbUser(null);
        config.setLinkCheckerDbPassword(null);
        languageCodeUtils = new LanguageCodeUtils(config);
    }

    @BeforeEach
    public void setup() throws Exception {
        if (Thread.currentThread().getName().equals("main")) {
            Thread.currentThread().setName("test-main");
        }

        fieldNameService = new FieldNameServiceImpl(config);
        marshaller = new VLOMarshaller();
    }

    public static String getTestFacetConceptFilePath() {
        try {
            return new File(ImporterTestcase.class.getResource("/facetConceptsTest.xml").toURI()).getAbsolutePath();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    static String getTestFacetsConfigFilePath() {
        try {
            return new File(ImporterTestcase.class.getResource("/facetConfigTest.xml").toURI()).getAbsolutePath();
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

    public static String getSilToIsoMapUrl() {
        return ImporterTestcase.class.getResource("/infra_clarin_eu/sil_to_iso6393.xml").toString();
    }

    private static String getCountryCodeComponentUrl() {
        return ImporterTestcase.class.getResource("/catalog_clarin_eu/c_1271859438104.xml").toString();
    }

    private static String getLanguage2LetterCodeUrl() {
        return ImporterTestcase.class.getResource("/catalog_clarin_eu/c_1271859438109.xml").toString();
    }

    private static String getLanguage3LetterCodeUrl() {
        return ImporterTestcase.class.getResource("/catalog_clarin_eu/c_1271859438110.xml").toString();
    }

}
