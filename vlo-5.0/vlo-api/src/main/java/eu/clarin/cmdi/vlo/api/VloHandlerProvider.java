/*
 * Copyright (C) 2023 twagoo
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
package eu.clarin.cmdi.vlo.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import static eu.clarin.cmdi.vlo.util.VloApiConstants.FILTER_QUERY_PARAMETER;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 *
 * @author twagoo
 */
@Slf4j
public abstract class VloHandlerProvider {

    protected final Splitter FQ_SPLITTER = Splitter.on(':').limit(2);

    protected <KEY, VALUE> Caffeine<KEY, VALUE> cacheBuilder(Long ttlSeconds, Long maxSize, boolean cacheStats) {
        final Caffeine builder = Caffeine.<KEY, VALUE>newBuilder();

        if (ttlSeconds != null) {
            builder.expireAfterWrite(Duration.ofSeconds(ttlSeconds));
        }

        if (maxSize != null) {
            builder.maximumSize(maxSize);
        }

        if (cacheStats) {
            builder.recordStats();
        }

        return builder;
    }

    protected Map<String, ? extends Iterable<String>> getFiltersFromRequest(ServerRequest request) {
        final List<String> fq = request.queryParams().get(FILTER_QUERY_PARAMETER);
        if (fq == null || fq.isEmpty()) {
            return Collections.emptyMap();
        } else {
            final ArrayListMultimap<String, String> map = ArrayListMultimap.<String, String>create();
            fq.forEach(fqVal -> {
                List<String> fqDecomposed = FQ_SPLITTER.splitToList(fqVal);
                if (fqDecomposed.size() == 2) {
                    map.put(fqDecomposed.get(0), fqDecomposed.get(1));
                } else {
                    log.warn("Ignoring invalid fq parameter: {}", fq);
                }
            });

            return map.asMap();
        }

    }
}
