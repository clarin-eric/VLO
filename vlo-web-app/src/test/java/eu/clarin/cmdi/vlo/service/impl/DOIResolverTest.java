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
package eu.clarin.cmdi.vlo.service.impl;

import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class DOIResolverTest {

    private DOIResolver instance;

    @Before
    public void setUp() {
        instance = new DOIResolver();
    }

    /**
     * Test of resolve method, of class DOIResolver.
     */
    @Test
    @Ignore("Depends on live DOI resolver")
    public void testResolve() {
        final URI input = URI.create("https://doi.org/10.5076/e-codices-csg-0961");
        final URI expected = URI.create("https://www.e-codices.ch/en/list/one/csg/0961");
        final URI result = instance.resolve(input);
        assertEquals("DOI should resolve to expected target", expected, result);
    }

    @Test
    public void testResolveNonDoi() {
        final URI input = URI.create("https://www.google.com");
        final URI result = instance.resolve(input);
        assertNull("Non-DOI should not resolve", result);
    }

}
