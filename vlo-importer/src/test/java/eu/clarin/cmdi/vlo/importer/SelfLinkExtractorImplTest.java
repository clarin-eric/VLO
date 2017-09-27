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

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.vlo.importer.processor.SelfLinkExtractor;
import eu.clarin.cmdi.vlo.importer.processor.SelfLinkExtractorImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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

    private static final String TEST_SELFLINK = "http://hdl.handle.net/10932/00-027B-9E8A-9300-0B01-E";
    private static final String TEST_RESOURCE = "/testRecords/oai_repos_ids_mannheim_de_clarin_ids_ab_000000.xml";
    private static final String TEST_RESOURCE_NO_SELFLINK = "/testRecords/oai_repos_ids_mannheim_de_clarin_ids_ab_000000-noselflink.xml";

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
        createTestFile(TEST_RESOURCE);
        String result = instance.extractMdSelfLink(testFile);
        assertEquals(TEST_SELFLINK, result);
    }

    /**
     * Test of extractMdSelfLink method, of class SelfLinkExtractorImpl.
     */
    @Test
    public void testExtractMdSelfLinkNoLink() throws Exception {
        createTestFile(TEST_RESOURCE_NO_SELFLINK);
        String result = instance.extractMdSelfLink(testFile);
        assertNull(result);
    }

    final private int performanceTestIterations = 100000;

    @Test
    @Ignore("Ignoring performance test")
    public void testPerformance() throws Exception {
        final Logger logger = LogManager.getLogger(SelfLinkExtractorImpl.class);
        final Level level = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            final long start = System.currentTimeMillis();
            runTestIterations(new SelfLinkExtractorImpl());
            final long time = System.currentTimeMillis() - start;
            System.out.printf("NEW: total time (2 * %d iterations): %d ms\n", performanceTestIterations, time);
        } finally {
            logger.setLevel(level);
        }
    }

    @Test
    @Ignore("Ignoring performance test")
    public void testPerformanceOld() throws Exception {
        final Logger logger = LogManager.getLogger(OldSelfLinkExtractor.class);
        final Level level = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            final long start = System.currentTimeMillis();
            runTestIterations(new OldSelfLinkExtractor());
            final long time = System.currentTimeMillis() - start;
            System.out.printf("OLD: total time (2 * %d iterations): %d ms\n", performanceTestIterations, time);
        } finally {
            logger.setLevel(level);
        }
    }

    private void runTestIterations(SelfLinkExtractor instance) throws IOException {
        createTestFile(TEST_RESOURCE);
        for (int i = 0; i < performanceTestIterations; i++) {
            assertEquals(TEST_SELFLINK, instance.extractMdSelfLink(testFile));
        }
        testFile.delete();

        createTestFile(TEST_RESOURCE_NO_SELFLINK);
        for (int i = 0; i < performanceTestIterations; i++) {
            assertNull(instance.extractMdSelfLink(testFile));
        }
    }

    private static class OldSelfLinkExtractor implements SelfLinkExtractor {

        @Override
        public String extractMdSelfLink(File file) throws IOException {

            try {
                final VTDGen vg = new VTDGen();
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    vg.setDoc(IOUtils.toByteArray(fileInputStream));
                    vg.parse(true);
                }
                final VTDNav nav = vg.getNav();
                nav.toElement(VTDNav.ROOT);
                AutoPilot ap = new AutoPilot(nav);
                setNameSpace(ap, null);
                ap.selectXPath("/cmd:CMD/cmd:Header/cmd:MdSelfLink/text()");
                int index = ap.evalXPath();

                String mdSelfLink = null;
                if (index != -1) {
                    mdSelfLink = nav.toString(index).trim();
                }
                return mdSelfLink;
            } catch (VTDException ex) {
                return null;
            }

        }

        /**
         * Setting namespace for Autopilot ap
         *
         * @param ap
         */
        private void setNameSpace(AutoPilot ap, String profileId) {
            ap.declareXPathNameSpace("cmd", "http://www.clarin.eu/cmd/1");
            if (profileId != null) {
                ap.declareXPathNameSpace("cmdp", "http://www.clarin.eu/cmd/1/profiles/" + profileId);
            }
        }

    }

}
