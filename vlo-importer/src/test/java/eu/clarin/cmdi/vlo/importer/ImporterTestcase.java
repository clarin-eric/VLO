package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

public abstract class ImporterTestcase {
    private File testDir;

    protected File createCmdiFile(String name, String content) throws IOException {
        File file = File.createTempFile(name, ".cmdi", testDir);
        FileUtils.writeStringToFile(file, content, "UTF-8");
        return file;
    }

    @After
    public void cleanup() {
        FileUtils.deleteQuietly(testDir);
    }

    @Before
    public void setup() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");
        testDir = new File(baseTempPath + File.separator + "testRegistry_" + System.currentTimeMillis());
        testDir.mkdir();
        testDir.deleteOnExit();
        
        // read the configuration defined in the packaged configuration file
        VloConfig.readPackagedConfig();

        // optionally, modify the configuration here
        
        VloConfig.setComponentRegistryRESTURL("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/");
    }

}
