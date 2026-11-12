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

import eu.clarin.cmdi.vlo.service.PIDResolver;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;
import nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author twagoo
 */
public class UriResolverImplTest {

    private final Mockery context = new JUnit4Mockery();
    private UriResolverImpl instance;
    private PIDResolver handleClient;
    private PIDResolver doiClient;

    @BeforeEach
    public void setUp() {
        handleClient = context.mock(PIDResolver.class, "handleResolver");
        doiClient = context.mock(PIDResolver.class, "doiResolver");
        instance = new UriResolverImpl(handleClient, doiClient);
    }

    public void testCanResolve() {
        Stream.of("hdl:1234/5678", "http://hdl.handle.net/1234/5678", "doi:1234/5678", "https://doi.org/1234/5678")
                .forEach(p -> assertTrue(instance.canResolve(p), "can resolve " + p));
        Stream.of("http://www.clarin.eu", "/relative", "zzzz")
                .forEach(p -> assertFalse(instance.canResolve(p), "cannot resolve " + p));
    }

    /**
     * Test of resolve method, of class HandleClientUriResolverImpl.
     */
    @Test
    public void testResolveNonHandle() {
        Optional<String> result = instance.resolve("http://www.clarin.eu");
        assertNotNull(result);
        assertTrue(result.isEmpty());
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
        String result = instance.resolve("hdl:1234/5678").orElseThrow();
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
        String result = instance.resolve("http://hdl.handle.net/1234/5678").orElseThrow();
        assertEquals("http://www.clarin.eu", result);
    }

    /**
     * Test of resolve method, of class HandleClientUriResolverImpl.
     */
    @Test
    public void testResolveDoiScheme() throws InvalidHandleException {
        context.checking(new Expectations() {
            {
                oneOf(doiClient).resolve(URI.create("doi:1234/5678"));
                will(returnValue(URI.create("http://www.clarin.eu")));
            }
        });
        String result = instance.resolve("doi:1234/5678").orElseThrow();
        assertEquals("http://www.clarin.eu", result);
    }

    /**
     * Test of resolve method, of class HandleClientUriResolverImpl.
     */
    @Test
    public void testResolveDoiUrl() throws InvalidHandleException {
        context.checking(new Expectations() {
            {
                oneOf(doiClient).resolve(URI.create("doi:1234/5678"));
                will(returnValue(URI.create("http://www.clarin.eu")));
            }
        });
        String result = instance.resolve("https://doi.org/1234/5678").orElseThrow();
        assertEquals("http://www.clarin.eu", result);
    }

}
