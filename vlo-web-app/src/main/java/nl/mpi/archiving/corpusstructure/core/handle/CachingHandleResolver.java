/*
 * Copyright (C) 2014 Max Planck Institute for Psycholinguistics
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
package nl.mpi.archiving.corpusstructure.core.handle;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

/**
 * A wrapper for the handle resolvers that stores handle - URL mappings in a
 * cache that expires entries after a configurable amount of time.
 *
 * <p>
 * TODO: add automatic periodic background refreshing using refreshAfterWrite
 * </p>
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CachingHandleResolver implements HandleResolver {

    private final static Logger logger = LoggerFactory.getLogger(CachingHandleResolver.class);

    private final HandleResolver inner;
    private final Cache cache;

    /**
     * Constructs a wrapper for the provided resolver that caches entries in the
     * provided cache.
     *
     * @param resolver inner resolver to use
     * @param cache cache for resolved entries
     */
    public CachingHandleResolver(HandleResolver resolver, Cache cache) {
        logger.info("Results of the handle resolver [{}] will be cached in [{}]", resolver, cache.getName());
        this.inner = resolver;
        this.cache = cache;
    }

    @Override
    public URI resolve(URI uri) throws InvalidHandleException {
        try {
            return cache.get(uri, () -> inner.resolve(uri));
        } catch (Cache.ValueRetrievalException ex) {
            if (ex.getCause() instanceof InvalidHandleException) {
                throw (InvalidHandleException) ex.getCause();
            } else {
                logger.error("Error while getting resolved handle from cache", ex);
            }
        } catch (RuntimeException ex) {
            logger.error("Error while getting resolved handle from cache", ex);
        }

        //if we get her, an error has occurred
        return inner.resolve(uri);
    }

}
