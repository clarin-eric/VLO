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
package eu.clarin.cmdi.vlo.wicket.model;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class ResolvingLinkModelTest {

    private final Mockery context = new JUnit4Mockery();

    @Test
    public void testGetObjectResolvableRelative() {
        //page on same level
        testResolves("http://clarin.eu/test/other", "http://clarin.eu/test/this", "other");
        
        //path on same level
        testResolves("http://clarin.eu/test/other/page", "http://clarin.eu/test/this", "other/page");
        
        //path as child
        testResolves("http://clarin.eu/test/this/other/page", "http://clarin.eu/test/this/", "other/page");
        
        //root path
        testResolves("http://clarin.eu/other/page", "http://clarin.eu/test/this/", "/other/page");
    }

    @Test
    public void testGetObjectResolvableAbsolute() {
        //absolute URLs
        testResolves("http://other.eu/test/that", "http://clarin.eu/test/this", "http://other.eu/test/that");
        testResolves("HTTTP://OTHER.EU/test/that", "http://clarin.eu/test/this", "HTTTP://OTHER.EU/test/that");
        
        //should work also against null
        testResolves("http://other.eu/test/that", null, "http://other.eu/test/that");
        
        //should work also against handle
        testResolves("http://other.eu/test/that", "http://hdl.handle.net/1234/5678", "http://other.eu/test/that");
    }

    @Test
    public void testGetObjectNotResolvableHandle() {
        //resolve relative against handle URI
        testResolvesNull("hdl:1234/5678", "other");
        
        //resolve relative against handle proxy
        testResolvesNull("http://hdl.handle.net/1234/5678", "other");
        testResolvesNull("HTTP://HDL.HANDLE.NET/1234/5678", "other");
        
        //resolve relative against handle proxy (https)
        testResolvesNull("https://hdl.handle.net/1234/5678", "other");
        testResolvesNull("HTTPS://HDL.HANDLE.NET/1234/5678", "other");
    }
    
    @Test
    public void testGetObjectNotResolvableRelativeObject() {
        //resolve against relative
        testResolvesNull("/test/this", "other");
    }

    @Test
    public void testGetObjectNotResolvableInvalidURI() {
        testResolvesNull("http__:bla", "other");
    }

    private static void testResolves(String target, String object, String subject) {
        ResolvingLinkModel model = new ResolvingLinkModel(Model.of(object), Model.of(subject));
        assertEquals(String.format("Expected [%1s] to resolve to [%2s] against [%3s]", subject, target, object), target, model.getObject());
    }

    private static void testResolvesNull(String object, String subject) {
        ResolvingLinkModel model = new ResolvingLinkModel(Model.of(object), Model.of(subject));
        assertNull(String.format("Expected [%1s] to NOT resolve against [%3s]", subject, object), model.getObject());
    }

    /**
     * Test of detach method, of class ResolvingLinkModel.
     */
    @Test
    public void testDetach() {
        final IModel subject = context.mock(IModel.class, "subjectModel");
        final IModel object = context.mock(IModel.class, "objectModel");
        context.checking(new Expectations() {
            {
                oneOf(subject).detach();
                oneOf(object).detach();
            }
        });
        ResolvingLinkModel instance = new ResolvingLinkModel(subject, object);
        instance.detach();
    }

}
