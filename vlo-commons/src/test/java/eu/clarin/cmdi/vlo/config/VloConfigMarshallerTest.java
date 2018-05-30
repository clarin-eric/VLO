/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.config;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class VloConfigMarshallerTest {

    private final static String VLO_CONFIG_FILE = "/VloConfig.xml";
    private final static Logger logger = LoggerFactory.getLogger(VloConfigMarshallerTest.class);
    private VloConfigMarshaller instance;
    private Properties testProps;

    @Before
    public void setUp() throws Exception {
        instance = new VloConfigMarshaller();
        testProps = new Properties();
        testProps.load(getClass().getResourceAsStream("/vloconfig.properties"));
    }

    /**
     * Test of marshal method, of class VloConfigMarshaller.
     */
    @Test
    public void testUnmarshal() throws Exception {
        try (InputStream configFile = getClass().getResourceAsStream(VLO_CONFIG_FILE)) {
            VloConfig config = instance.unmarshal(new StreamSource(configFile, getClass().getResource(VLO_CONFIG_FILE).toString()));

            assertNotNull(config);
            assertEquals(testProps.getProperty("solrUrl"), config.getSolrUrl());
            assertEquals(12, config.getFacetFieldNames().size());

            assertEquals(4, config.getAvailabilityValues().size());
            assertEquals("Public", config.getAvailabilityValues().get(0).getDisplayValue());
        }
    }

    @Test
    public void testDefaultConfigValidity() throws Exception {
        final VloConfigMarshaller failingOnValidationErrorInstance = new VloConfigMarshaller() {
            @Override
            protected ValidationEventHandler getConfigValidationEventHandler() {
                return (event) -> {
                    fail("Validation error while unmarshalling default configuration:" + event.getMessage());
                    return false;
                };
            }

        };
        try (InputStream configFile = getClass().getResourceAsStream(VLO_CONFIG_FILE)) {
            VloConfig config = failingOnValidationErrorInstance.unmarshal(new StreamSource(configFile, getClass().getResource(VLO_CONFIG_FILE).toString()));
        }
    }

    /**
     * Test of marshal method, of class VloConfigMarshaller.
     */
    @Test
    public void testMarshal() throws Exception {
        final VloConfig config = new VloConfig();
        config.setSolrUrl("http://server/solr");
        config.setDataRoots(Arrays.asList(new DataRoot("originName", new File("rootFile"), "prefix", "toStrip", Boolean.FALSE)));
        config.setFacetFieldKeys(Arrays.asList("collection", "country", "continent"));
        config.setAvailabilityValues(Arrays.asList(
                new FieldValueDescriptor("PUB", "Public", "Description for public"),
                new FieldValueDescriptor("ACA", "Academic", "Description for academic")
        ));
        final StringWriter sw = new StringWriter();
        instance.marshal(config, new StreamResult(sw));
        logger.debug(sw.toString());
    }

}
