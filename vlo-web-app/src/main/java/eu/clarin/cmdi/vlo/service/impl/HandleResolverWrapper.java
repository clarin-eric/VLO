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

import eu.clarin.cmdi.vlo.service.PIDResolver;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import nl.mpi.archiving.corpusstructure.core.handle.HandleResolver;
import nl.mpi.archiving.corpusstructure.core.handle.InvalidHandleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class HandleResolverWrapper implements PIDResolver {

    private final static Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final static Logger logger = LoggerFactory.getLogger(HandleResolverWrapper.class);
    private final HandleResolver handleResolver;
    private Optional<Duration> timeout;

    /**
     * Construct with default timeout
     *
     * @param handleResolver resolver to wrap
     */
    public HandleResolverWrapper(HandleResolver handleResolver) {
        this(handleResolver, DEFAULT_TIMEOUT);
    }

    /**
     * Construct with custom timeout
     *
     * @param handleResolver resolver to wrap
     * @param timeout timeout for lookup, cannot be null
     */
    public HandleResolverWrapper(HandleResolver handleResolver, Duration timeout) {
        this(handleResolver, Optional.of(timeout));
    }

    /**
     * Construct with custom timeout or no timeout
     *
     * @param handleResolver resolver to wrap
     * @param timeout timeout for lookup, set to empty for no timeout
     */
    public HandleResolverWrapper(HandleResolver handleResolver, Optional<Duration> timeout) {
        this.handleResolver = handleResolver;
        this.timeout = timeout;
    }

    @Override
    public URI resolve(URI uri) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            final Future<URI> future = executor.<URI>submit(() -> {
                try {
                    return handleResolver.resolve(uri);
                } catch (InvalidHandleException ex) {
                    logger.error("Cannot resolve", ex);
                    return null;
                }
            });

            try {
                if (timeout.isPresent()) {
                    // get with timeout
                    return future.get(timeout.get().toMillis(), TimeUnit.MILLISECONDS);
                } else {
                    return future.get();
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.warn("Timeout, interrupted or error while resolving handle {}", uri, e);
                return null;
            }

        } finally {
            executor.shutdownNow();
        }
    }

    public void setTimeout(Duration timeout) {
        this.timeout = Optional.ofNullable(timeout);
    }

}
