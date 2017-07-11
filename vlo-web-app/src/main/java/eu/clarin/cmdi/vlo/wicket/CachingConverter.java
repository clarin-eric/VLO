/*
 * Copyright (C) 2015 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * Converter that wraps an arbitrary converter and caches the conversion output
 * so that each conversion in a certain direction given a set of a value and
 * locale or string representation and locale happens only once.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 * @param <C> The object to convert from and to String
 * @see Cache
 */
public class CachingConverter<C> implements IConverter<C> {

    private final IConverter<C> inner;

    private final LoadingCache<Key<String, Locale>, C> toObjectCache = CacheBuilder.newBuilder().
            build(
                    new CacheLoader<Key<String, Locale>, C>() {

                        @Override
                        public C load(Key<String, Locale> key) throws Exception {
                            return inner.convertToObject(key.getKey1(), key.getKey2());
                        }
                    }
            );

    private final LoadingCache<Key<C, Locale>, String> toStringCache = CacheBuilder.newBuilder().
            build(
                    new CacheLoader<Key<C, Locale>, String>() {

                        @Override
                        public String load(Key<C, Locale> key) throws Exception {
                            return inner.convertToString(key.getKey1(), key.getKey2());
                        }
                    }
            );

    /**
     *
     * @param converter converter to cache conversion results for
     */
    public CachingConverter(IConverter<C> converter) {
        this.inner = converter;
    }

    /**
     * Convenience factory method for wrapping a converter
     *
     * @param <C> The object to convert from and to String
     * @param inner converter to wrap, can be null
     * @return a new {@link CachingConverter} instance wrapping the inner
     * converter, or null if inner is null
     */
    public static <C> IConverter<C> wrap(IConverter<C> inner) {
        if (inner == null) {
            return null;
        } else {
            return new CachingConverter(inner);
        }
    }

    @Override
    public C convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return toObjectCache.get(new Key<>(value, locale));
        } catch (ExecutionException ex) {
            throw new ConversionException(ex);
        }
    }

    @Override
    public String convertToString(C value, Locale locale) {
        try {
            return toStringCache.get(new Key<>(value, locale));
        } catch (ExecutionException ex) {
            throw new ConversionException(ex);
        }
    }

    private static class Key<K1, K2> {

        private final K1 key1;
        private final K2 key2;

        public Key(K1 key1, K2 key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        public K1 getKey1() {
            return key1;
        }

        public K2 getKey2() {
            return key2;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + Objects.hashCode(this.key1);
            hash = 41 * hash + Objects.hashCode(this.key2);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key<?, ?> other = (Key<?, ?>) obj;
            if (!Objects.equals(this.key1, other.key1)) {
                return false;
            }
            if (!Objects.equals(this.key2, other.key2)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.format("{%s, %s}", key1.toString(), key2.toString());
        }

    }

}
