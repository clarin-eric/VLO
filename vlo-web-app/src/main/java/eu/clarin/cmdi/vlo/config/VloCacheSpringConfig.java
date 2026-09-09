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

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central configuration of the application's caches, backed by Caffeine and
 * exposed through the Spring cache abstraction.
 *
 */
@Configuration
@EnableCaching
public class VloCacheSpringConfig {

    /**
     * Cache of handle (PID) to target URI resolutions
     */
    public static final String HANDLE_RESOLUTION_CACHE = "handleResolution";

    /**
     * Cache of the centre registry endpoint provider list
     */
    public static final String CENTRE_ENDPOINTS_CACHE = "centreEndpoints";

    /**
     * Handle resolution cache expiry in seconds
     */
    private static final Duration HANDLE_CACHE_EXPIRY = Duration.ofHours(1);
    private static final long HANDLE_CACHE_MAX_SIZE = 20_000;
    private static final Duration ENDPOINTS_CACHE_EXPIRY = Duration.ofHours(2);

    @Bean
    public CacheManager cacheManager() {
        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                handleResolutionCache(),
                centreEndpointsCache()));
        return cacheManager;
    }

    private CaffeineCache handleResolutionCache() {
        return new CaffeineCache(HANDLE_RESOLUTION_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(HANDLE_CACHE_MAX_SIZE)
                        .expireAfterWrite(HANDLE_CACHE_EXPIRY)
                        .recordStats()
                        .build(),
                // a handle that does not resolve yields null; caching that
                // prevents a repeated external call for every occurrence
                true);
    }

    private CaffeineCache centreEndpointsCache() {
        return new CaffeineCache(CENTRE_ENDPOINTS_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(ENDPOINTS_CACHE_EXPIRY)
                        .recordStats()
                        .build(),
                false);
    }

}
