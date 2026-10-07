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

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metrics for the web application, shipped to the same statsd collector.
 *
 * <p>
 * Without a statsd host configured the registry is a simple in-memory one.
 * The size of these registry will be a bounded memory by the number of tags
 * </p>
 *
 */
@Configuration
public class VloMetricsSpringConfig {

    private final static Logger logger = LoggerFactory.getLogger(VloMetricsSpringConfig.class);
    private final static String METRIC_SUFFIX = ".webapp.";

    @Inject
    private VloConfig vloConfig;

    private StatsdMeterRegistry statsdRegistry;

    @Bean
    public MeterRegistry meterRegistry() {
        final String host = vloConfig.getStatsdHost();
        if (host == null || host.isEmpty()) {
            logger.info("No statsd host configured; metrics are recorded in memory only");
            return new SimpleMeterRegistry();
        }

        logger.info("Reporting metrics to statsd at {}:{} under '{}{}'", host, vloConfig.getStatsdPort(), vloConfig.getStatsdPrefix(), METRIC_SUFFIX);
        statsdRegistry = new StatsdMeterRegistry(new StatsdConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public StatsdFlavor flavor() {
                return StatsdFlavor.ETSY;
            }

            @Override
            public String host() {
                return host;
            }

            @Override
            public int port() {
                return vloConfig.getStatsdPort();
            }
        }, Clock.SYSTEM);

        final String prefix = vloConfig.getStatsdPrefix() + METRIC_SUFFIX;
        statsdRegistry.config().meterFilter(new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                return id.withName(prefix + id.getName());
            }
        });
        return statsdRegistry;
    }

    /**
     * Binds the statistics that the caches already record (see
     * {@link VloCacheSpringConfig}) to the registry.
     *
     * @param registry registry we send to
     * @param cacheManager spring's cache-manager
     * @return callback that performs the binding once the context is ready
     */
    @Bean
    public InitializingBean cacheMetrics(MeterRegistry registry, CacheManager cacheManager) {
        return () -> cacheManager.getCacheNames().forEach(name -> {
            final Cache cache = cacheManager.getCache(name);
            if (cache instanceof CaffeineCache caffeineCache) {
                CaffeineCacheMetrics.monitor(registry, caffeineCache.getNativeCache(), name);
                logger.debug("Reporting metrics for cache {}", name);
            }
        });
    }

    @PreDestroy
    public void close() {
        if (statsdRegistry != null) {
            statsdRegistry.close();
        }
    }

}
