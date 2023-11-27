/*
 * Copyright (C) 2023 CLARIN
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
package eu.clarin.cmdi.vlo.service.impl;

import java.net.URL;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class XmlTransformationServiceImplTest {

    private XmlTransformationServiceImpl instance;

    public XmlTransformationServiceImplTest() {
    }

    @Before
    public void setUp() {
        final Source xsltSource = new StreamSource(getClass().getResourceAsStream("/cmdi2xhtml.xsl"));
        final Properties transformationProperties = new Properties();
        transformationProperties.setProperty(OutputKeys.METHOD, "html");
        transformationProperties.setProperty(OutputKeys.INDENT, "no");
        transformationProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
        this.instance = new XmlTransformationServiceImpl(xsltSource, transformationProperties);
    }

    @After
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of init method, of class XmlTransformationServiceImpl.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testInit() throws Exception {
        instance.init();
        final Templates templates = instance.getTemplates();
        assertNotNull(templates);
        Transformer transformer = templates.newTransformer();
        assertNotNull(transformer);
    }

    /**
     * Test of transformXml method, of class XmlTransformationServiceImpl.
     */
    @Test
    public void testTransformXml() throws Exception {
        instance.init();
        final String transformationResult = instance.transformXml(getClass().getResource("/test-instance.cmdi"));
        assertNotNull(transformationResult);
    }

}
