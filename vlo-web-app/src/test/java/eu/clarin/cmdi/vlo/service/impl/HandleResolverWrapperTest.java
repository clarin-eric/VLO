/*
 * Copyright (C) 2022 CLARIN
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
import java.time.Duration;
import java.util.Optional;
import nl.mpi.archiving.corpusstructure.core.handle.HandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException;
import org.hamcrest.Description;
import static org.jmock.AbstractExpectations.doAll;
import static org.jmock.AbstractExpectations.returnValue;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class HandleResolverWrapperTest {

    private final Mockery context = new JUnit4Mockery() {
        {
            setThreadingPolicy(new Synchroniser());
        }
    };
    private final URI testUri = URI.create("https://hdl.handle.net/1234/test-5678");
    private final URI testUriResolved = URI.create("https://www.clarin.eu/test-5678");

    HandleResolver resolver;

    @Before
    public void setUp() {
        resolver = context.mock(HandleResolver.class);
    }

    /**
     * Test of resolve method, of class HandleResolverWrapper.
     *
     * @throws
     * nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException
     */
    @Test
    public void testResolve() throws InvalidHandleException {
        //no timeout
        final HandleResolverWrapper instance = new HandleResolverWrapper(resolver, Optional.empty());

        context.checking(new Expectations() {
            {
                oneOf(resolver).resolve(testUri);
                will(returnValue(testUriResolved));
            }

        });

        final URI result = instance.resolve(testUri);
        assertEquals(testUriResolved, result);
    }

    /**
     * Test of resolve method, of class HandleResolverWrapper.
     *
     * @throws
     * nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException
     */
    @Test
    public void testResolveTimeout() throws InvalidHandleException {
        //no timeout
        final HandleResolverWrapper instance = new HandleResolverWrapper(resolver, Optional.of(Duration.ofMillis(50)));

        context.checking(new Expectations() {
            {
                oneOf(resolver).resolve(testUri);
                will(doAll(sleep(5000), returnValue(testUriResolved)));
            }

        });

        final URI result = instance.resolve(testUri);
        assertNull(result);
    }

    /**
     * Test of resolve method, of class HandleResolverWrapper.
     *
     * @throws
     * nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException
     */
    @Test
    public void testResolveException() throws InvalidHandleException {
        //no timeout
        final HandleResolverWrapper instance = new HandleResolverWrapper(resolver, Optional.empty());

        context.checking(new Expectations() {
            {
                oneOf(resolver).resolve(testUri);
                will(throwException(new RuntimeException("Mocked resolver error")));
            }

        });

        final URI result = instance.resolve(testUri);
        assertNull(result);
    }

    private static <T> Action sleep(long sleepTime) {
        return new Action() {
            @Override
            public void describeTo(Description d) {
                d.appendText("sleeps for ").appendValue(sleepTime).appendText("ms");
            }

            @Override
            public Object invoke(Invocation invctn) throws Throwable {
                Thread.sleep(sleepTime);
                return null;
            }
        };

    }

}
