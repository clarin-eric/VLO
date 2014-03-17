/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Model that stores a map entry with a serialisable key and value. The returned
 * map should be assumed immutable.
 *
 * @author twagoo
 * @param <K> key type
 * @param <V> value type
 */
public class CollectionMapEntryModel<K extends Serializable, V extends Serializable> extends LoadableDetachableModel<Entry<K, Collection<V>>> {

    private final K key;
    private final Collection<V> value;

    public CollectionMapEntryModel(Entry<K, Collection<V>> entry) {
        super(entry);
        key = entry.getKey();

        final Collection<V> entryValue = entry.getValue();
        if (entryValue instanceof Serializable) {
            value = entryValue;
        } else {
            // copy to a serialisable collection
            value = Lists.newArrayList(entryValue);
        }
    }

    @Override
    protected Entry<K, Collection<V>> load() {
        return Maps.immutableEntry(key, value);
    }

}
