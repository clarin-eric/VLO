/*
 * Copyright (C) 2026 CLARIN
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

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import nl.mpi.archiving.corpusstructure.core.handle.CachingHandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.HandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Verifies that the caches declared by {@link VloCacheSpringConfig} are
 * available under the names the application uses, and that the caching
 * decorators built on top of them behave as expected.
 *
 */
public class VloCacheSpringConfigTest {

    private AnnotationConfigApplicationContext context;
    private CacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        context = new AnnotationConfigApplicationContext(VloCacheSpringConfig.class);
        cacheManager = context.getBean(CacheManager.class);
    }

    @AfterEach
    public void tearDown() {
        context.close();
    }

    @Test
    public void testCachesAreRegistered() {
        assertNotNull(cacheManager.getCache(VloCacheSpringConfig.HANDLE_RESOLUTION_CACHE),
                "handle resolution cache should be registered");
        assertNotNull(cacheManager.getCache(VloCacheSpringConfig.CENTRE_ENDPOINTS_CACHE),
                "centre endpoints cache should be registered");
    }

    @Test
    public void testHandleResolutionIsCached() throws InvalidHandleException {
        final URI handle = URI.create("hdl:1839/00-0000-0000-0000-0000-1");
        final URI target = URI.create("https://example.com/record/1");
        final AtomicInteger calls = new AtomicInteger();

        final HandleResolver resolver = new CachingHandleResolver(uri -> {
            calls.incrementAndGet();
            return target;
        }, Objects.requireNonNull(cacheManager.getCache(VloCacheSpringConfig.HANDLE_RESOLUTION_CACHE)));

        assertSame(target, resolver.resolve(handle));
        assertSame(target, resolver.resolve(handle));
        assertSame(target, resolver.resolve(handle));
        assertEquals(1, calls.get(), "inner resolver should only be called once");
    }

    @Test
    public void testUnresolvableHandleIsCached() throws InvalidHandleException {
        final URI handle = URI.create("hdl:1839/does-not-resolve");
        final AtomicInteger calls = new AtomicInteger();

        final HandleResolver resolver = new CachingHandleResolver(uri -> {
            calls.incrementAndGet();
            return null;
        }, Objects.requireNonNull(cacheManager.getCache(VloCacheSpringConfig.HANDLE_RESOLUTION_CACHE)));

        assertNull(resolver.resolve(handle));
        assertNull(resolver.resolve(handle));
        assertEquals(1, calls.get(), "null resolution should be cached");
    }
}
