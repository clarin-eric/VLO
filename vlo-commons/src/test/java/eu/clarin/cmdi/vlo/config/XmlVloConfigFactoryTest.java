/*
 * Copyright (C) 2020 CLARIN
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

import java.io.InputStream;
import java.net.URI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class XmlVloConfigFactoryTest {
    
    @Test
    public void test() throws Exception {
        XmlVloConfigFactory instance = new XmlVloConfigFactory(getClass().getResource("/VloConfig-test.xml"));
        VloConfig config = instance.newConfig();
        assertEquals("facetConcepts-test.xml", config.getFacetConceptsFile());
    }
    
    @Test
    public void testUTF8() throws Exception {
        XmlVloConfigFactory instance = new XmlVloConfigFactory(getClass().getResource("/VloConfig-utf8.xml"));
        VloConfig config = instance.newConfig();
        assertEquals("facetCönceptš.xml", config.getFacetConceptsFile());
    }
    
}
