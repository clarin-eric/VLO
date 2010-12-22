package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class ImporterTestcase {
    private static File testDir;
    
    protected FacetMapping getOlacFacetMap() {
        BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { ImporterConfig.CONFIG_FILE });
        FacetMapping facetMapping = (FacetMapping) factory.getBean("olacMapping");
        return facetMapping;
    }

    protected FacetMapping getIMDIFacetMap() {
        BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { ImporterConfig.CONFIG_FILE });
        FacetMapping facetMapping = (FacetMapping) factory.getBean("imdiMapping");
        return facetMapping;
    }

    protected FacetMapping getLrtFacetMap() {
        BeanFactory factory = new ClassPathXmlApplicationContext(new String[] { ImporterConfig.CONFIG_FILE });
        FacetMapping facetMapping = (FacetMapping) factory.getBean("lrtMapping");
        return facetMapping;
    }
    
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
    }

}
