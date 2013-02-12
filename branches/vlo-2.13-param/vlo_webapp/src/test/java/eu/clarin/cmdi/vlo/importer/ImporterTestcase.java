package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.IOException;

import eu.clarin.cmdi.vlo.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class ImporterTestcase {
    private static File testDir;

    protected File createCmdiFile(String name, String content) throws IOException {
        File file = File.createTempFile(name, ".cmdi", testDir);
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }

    @AfterClass
    public static void cleanup() {
        FileUtils.deleteQuietly(testDir);
    }

    @BeforeClass
    public static void setup() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        testDir = new File(baseTempPath + File.separator + "testRegistry_" + System.currentTimeMillis());
        testDir.mkdir();
        testDir.deleteOnExit();
        Configuration.getInstance().setComponentRegistryRESTURL("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/");
    }

}
