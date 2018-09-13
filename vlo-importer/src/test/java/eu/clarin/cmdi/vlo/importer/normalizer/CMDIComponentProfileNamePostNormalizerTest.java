/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer.normalizer;

import eu.clarin.cmdi.vlo.importer.ImporterTestcase;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class CMDIComponentProfileNamePostNormalizerTest extends ImporterTestcase {
    
    private final static Logger LOG = LoggerFactory.getLogger(CMDIComponentProfileNamePostNormalizerTest.class);
    private CMDIComponentProfileNamePostNormalizer instance;
    
    @Before
    public void setUp() throws Exception {
        instance = new CMDIComponentProfileNamePostNormalizer(config) {
            @Override
            protected InputStream getInputStream(String profileId) throws IOException {
                final String resourceName = "/testProfiles/" + profileId + ".xml";
                LOG.debug("Profile definition from static resources: {}", resourceName);
                final InputStream is = CMDIComponentProfileNamePostNormalizerTest.class.getResourceAsStream(resourceName);
                if (is == null) {
                    LOG.info("No static resource {}, using default method" + resourceName);
                    return super.getInputStream(profileId);
                } else {
                    return is;
                }
            }
            
        };
    }

    /**
     * Test of process method, of class CMDIComponentProfileNamePostNormalizer.
     */
    @Test
    public void testProcess() {
        List<String> result = instance.process("clarin.eu:cr1:p_1282306194508", null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("teiHeader", result.get(0));
    }

    /**
     * Test of process method, of class CMDIComponentProfileNamePostNormalizer.
     */
    @Test
    public void testProcessNonExistingProfile() {
        List<String> result = instance.process("clarin.xx:cr1:p_1282zzz", null);
        assertNull(result);
    }
    
}
