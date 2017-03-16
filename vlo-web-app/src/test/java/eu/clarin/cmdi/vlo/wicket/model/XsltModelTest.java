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
package eu.clarin.cmdi.vlo.wicket.model;

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.collect.Lists;
import eu.clarin.cmdi.vlo.service.XmlTransformationService;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class XsltModelTest {
    
    private final Mockery context = new JUnit4Mockery();
    private IModel<List<URL>> urlModel;
    private XsltModel instance;
    private XmlTransformationService service;
    
    @Before
    public void setUp() {
        service = context.mock(XmlTransformationService.class);
        urlModel = new ListModel<>();
        instance = new XsltModel(urlModel) {
            
            @Override
            protected XmlTransformationService getTransformationService() {
                return service;
            }
            
        };
    }

    /**
     * Test of load method, of class XsltModel.
     */
    @Test
    public void testLoadNull() {
        urlModel.setObject(null);
        String result = instance.load();
        assertEquals("", result);
    }

    /**
     * Test of load method, of class XsltModel.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLoad() throws Exception {
        final URL url = new URL("http://document/to/transform.xml");
        urlModel.setObject(Arrays.asList(url));
        
        context.checking(new Expectations() {
            {
                oneOf(service).transformXml(url);
                will(returnValue("transformation output"));
            }
        });
        final String result = instance.load();
        assertEquals("transformation output", result);
    }

    /**
     * Test of load method, of class XsltModel.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLoadTransformerException() throws Exception {
        final URL url = new URL("http://document/to/transform.xml");
        urlModel.setObject(Arrays.asList(url));
        
        context.checking(new Expectations() {
            {
                oneOf(service).transformXml(url);
                will(throwException(new TransformerException("intentional testing exception")));
            }
        });
        final String result = instance.load();
        assertTrue(result.contains("Could not load"));
    }
}
