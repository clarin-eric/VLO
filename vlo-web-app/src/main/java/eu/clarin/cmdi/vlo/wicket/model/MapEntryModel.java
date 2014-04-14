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

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Map.Entry;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 * @param <K> key type
 * @param <V> value type
 */
public class MapEntryModel<K extends Serializable, V extends Serializable> implements IModel<Entry<K, V>> {

    private K key;
    private V value;

    public MapEntryModel(Entry<K, V> entry) {
        setObject(entry);
    }

    public MapEntryModel(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Entry<K, V> getObject() {
        return Maps.immutableEntry(key, value);
    }

    @Override
    public final void setObject(Entry<K, V> object) {
        key = object.getKey();
        value = object.getValue();
    }

    @Override
    public void detach() {
    }

}
