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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.Locale;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * Converter that wraps an arbitrary converter and caches the conversion output
 * so that each conversion in a certain direction given a set of a value and
 * locale or string representation and locale happens only once.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 * @param <C> The object to convert from and to String
 */
public class CachingConverter<C> implements IConverter<C> {

    private final IConverter<C> inner;

    private final LoadingCache<Key<String, Locale>, C> toObjectCache;
    private final LoadingCache<Key<C, Locale>, String> toStringCache;

    /**
     *
     * @param converter converter to cache conversion results for
     */
    public CachingConverter(IConverter<C> converter) {
        this.inner = converter;
        this.toObjectCache = Caffeine.newBuilder()
                .build(key -> inner.convertToObject(key.key1(), key.key2()));
        this.toStringCache = Caffeine.newBuilder()
                .build(key -> inner.convertToString(key.key1(), key.key2()));
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
            return new CachingConverter<>(inner);
        }
    }

    @Override
    public C convertToObject(String value, Locale locale) throws ConversionException {
        return toObjectCache.get(new Key<>(value, locale));
    }

    @Override
    public String convertToString(C value, Locale locale) {
        return toStringCache.get(new Key<>(value, locale));
    }

    private record Key<K1, K2>(K1 key1, K2 key2) {

        @Override
        public String toString() {
            return String.format("{%s, %s}", key1, key2);
        }
    }

}
