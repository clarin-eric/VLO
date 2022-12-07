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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for the handle resolvers that stores handle - URL mappings in a
 * cache that expires entries after a configurable amount of time.
 *
 * <p>
 * TODO: add automatic periodic background refreshing using {@link CacheBuilder#refreshAfterWrite(long, java.util.concurrent.TimeUnit)
 * }
 * and {@link CacheLoader#reload(java.lang.Object, java.lang.Object) } (see
 * {@link https://code.google.com/p/guava-libraries/wiki/CachesExplained#Refresh})
 * </p>
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CachingHandleResolver implements HandleResolver {

    private final static Logger logger = LoggerFactory.getLogger(CachingHandleResolver.class);

    private final HandleResolver inner;
    private final LoadingCache<URI, URI> cache;

    /**
     * Constructs a wrapper for the provided resolver that expires entries after
     * the specified amount of time.
     *
     * @param resolver inner resolver to use
     * @param expireTime expiry time for cached entries in seconds
     */
    public CachingHandleResolver(HandleResolver resolver, int expireTime) {
        logger.info("Results of the handle resolver [{}] will be cached for {} seconds", resolver, expireTime);
        this.inner = resolver;
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expireTime, TimeUnit.SECONDS)
                .build(new CacheLoader<URI, URI>() {

                    @Override
                    public URI load(URI uri) throws Exception {
                        return inner.resolve(uri);
                    }
                });
    }

    @Override
    public URI resolve(URI uri) throws InvalidHandleException {
        try {
            return cache.get(uri);
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof InvalidHandleException) {
                throw (InvalidHandleException) ex.getCause();
            } else {
                logger.error("Error while getting resolved handle from cache", ex);
            }
        } catch (Exception ex) {
            logger.error("Error while getting resolved handle from cache", ex);
        }

        //if we get her, an error has occurred
        return inner.resolve(uri);
    }

}
