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

import java.net.MalformedURLException;
import java.net.URL;
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
public class UrlFromStringModelTest {

    private final Mockery context = new JUnit4Mockery();

    /**
     * Test of getObject method, of class UrlFromStringModel.
     */
    @Test
    public void testGetObject() throws MalformedURLException {
        final Model<String> innerModel = Model.of("");
        final UrlFromStringModel instance = new UrlFromStringModel(innerModel);

        innerModel.setObject(null);
        assertNull(instance.getObject());
        
        innerModel.setObject("/my/file/location");
        assertEquals(new URL("file:/my/file/location"), instance.getObject());
        
        innerModel.setObject("http://my/external/location");
        assertEquals(new URL("http://my/external/location"), instance.getObject());
        
        innerModel.setObject("https://my/secure/location");
        assertEquals(new URL("https://my/secure/location"), instance.getObject());
    }

    /**
     * Test of setObject method, of class UrlFromStringModel.
     */
    @Test
    public void testSetObject() throws MalformedURLException {
        final Model<String> innerModel = Model.of("");
        final UrlFromStringModel instance = new UrlFromStringModel(innerModel);

        instance.setObject(null);
        assertNull(innerModel.getObject());

        instance.setObject(new URL("file:/my/file/location"));
        assertEquals("/my/file/location", innerModel.getObject());

        instance.setObject(new URL("http://my/external/location"));
        assertEquals("http://my/external/location", innerModel.getObject());
    }

    /**
     * Test of detach method, of class UrlFromStringModel.
     */
    @Test
    public void testDetach() {
        final IModel innerModel = context.mock(IModel.class);
        final UrlFromStringModel instance = new UrlFromStringModel(innerModel);

        context.checking(new Expectations() {
            {
                oneOf(innerModel).detach();
            }
        });
        instance.detach();

    }

}
