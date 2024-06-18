/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.vlo.service.centreregistry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class CachingEndPointProvidersService implements EndpointProvidersService {

    private final static Logger logger = LoggerFactory.getLogger(CachingEndPointProvidersService.class);
    private final static String CACHE_KEY = "CACHE_KEY";

    private final EndpointProvidersService service;
    private final LoadingCache<String, List<EndpointProvider>> cache;

    public CachingEndPointProvidersService(EndpointProvidersService service, Duration expirationTime) {
        this.service = service;
        cache = CacheBuilder.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(expirationTime)
                .build(
                        new CacheLoader<String, List<EndpointProvider>>() {
                    @Override
                    public List<EndpointProvider> load(String key) throws Exception {
                        return service.retrieveCentreEndpoints();
                    }
                });
    }

    @Override
    public List<EndpointProvider> retrieveCentreEndpoints() throws IOException {
        try {
            return cache.get(CACHE_KEY);
        } catch (ExecutionException ex) {
            logger.warn("Failed to retrieve endpoints list from cache; falling back to uncached service!", ex);
            return service.retrieveCentreEndpoints();
        }
    }

}
