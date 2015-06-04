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
package eu.clarin.cmdi.vlo.service.impl;

import java.net.URI;
import nl.mpi.archiving.corpusstructure.core.handle.HandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException;
import org.jmock.Expectations;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author twagoo
 */
public class UriResolverImplTest {

    private final Mockery context = new JUnit4Mockery();
    private UriResolverImpl instance;
    private HandleResolver handleClient;

    @Before
    public void setUp() {
        handleClient = context.mock(HandleResolver.class);
        instance = new UriResolverImpl(handleClient);
    }

    /**
     * Test of resolve method, of class HandleClientUriResolverImpl.
     */
    @Test
    public void testResolveNonHandle() {
        String result = instance.resolve("http://www.clarin.eu");
        assertEquals("http://www.clarin.eu", result);
    }

    /**
     * Test of resolve method, of class HandleClientUriResolverImpl.
     */
    @Test
    public void testResolveHandleScheme() throws InvalidHandleException {
        context.checking(new Expectations() {
            {
                oneOf(handleClient).resolve(URI.create("hdl:1234/5678"));
                will(returnValue(URI.create("http://www.clarin.eu")));
            }
        });
        String result = instance.resolve("hdl:1234/5678");
        assertEquals("http://www.clarin.eu", result);
    }

    /**
     * Test of resolve method, of class HandleClientUriResolverImpl.
     */
    @Test
    public void testResolveHandleProxy() throws InvalidHandleException {
        context.checking(new Expectations() {
            {
                oneOf(handleClient).resolve(URI.create("hdl:1234/5678"));
                will(returnValue(URI.create("http://www.clarin.eu")));
            }
        });
        String result = instance.resolve("http://hdl.handle.net/1234/5678");
        assertEquals("http://www.clarin.eu", result);
    }

}
