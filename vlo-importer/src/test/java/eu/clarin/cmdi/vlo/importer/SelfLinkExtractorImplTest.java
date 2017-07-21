/*
 * Copyright (C) 2017 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.vlo.importer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author twagoo
 */
public class SelfLinkExtractorImplTest {

    private File testFile;
    private SelfLinkExtractorImpl instance;

    public void createTestFile(String resource) throws IOException {
        //create test file
        testFile = File.createTempFile("selfLinkExtractor", "cmdi");
        final InputStream is = SelfLinkExtractorImplTest.class.getResourceAsStream(resource);
        try (FileWriter fileWriter = new FileWriter(testFile)) {
            IOUtils.copy(is, fileWriter, "UTF-8");
        }
    }

    @Before
    public void setUp() {
        instance = new SelfLinkExtractorImpl();
    }

    @After
    public void cleanUp() {
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    /**
     * Test of extractMdSelfLink method, of class SelfLinkExtractorImpl.
     */
    @Test
    public void testExtractMdSelfLink() throws Exception {
        createTestFile("/testRecords/oai_repos_ids_mannheim_de_clarin_ids_ab_000000.xml");
        String expResult = "http://hdl.handle.net/10932/00-027B-9E8A-9300-0B01-E";
        String result = instance.extractMdSelfLink(testFile);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractMdSelfLink method, of class SelfLinkExtractorImpl.
     */
    @Test
    public void testExtractMdSelfLinkNoLink() throws Exception {
        createTestFile("/testRecords/oai_repos_ids_mannheim_de_clarin_ids_ab_000000-noselflink.xml");
        String result = instance.extractMdSelfLink(testFile);
        assertNull(result);
    }

    @Test @Ignore("Performance test ignored")
    public void testPerformance() throws Exception{
        createTestFile("/testRecords/oai_repos_ids_mannheim_de_clarin_ids_ab_000000.xml");
        
        final long start = System.currentTimeMillis();
        final int iterations = 100000;
        for(int i=0;i<iterations;i++) {
            instance.extractMdSelfLink(testFile);
        }
        final long time = System.currentTimeMillis() - start;
        System.out.printf("Total time (%d iterations): %d ms\n", iterations, time);
    }

}
