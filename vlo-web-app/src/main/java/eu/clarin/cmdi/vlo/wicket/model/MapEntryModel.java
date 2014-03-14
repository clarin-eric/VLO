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

import java.util.Map.Entry;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 * @param <K> key type
 * @param <V> value type
 */
public class MapEntryModel<K, V> implements IModel<Entry<K, V>> {

    private Entry<K, V> entry;

    public MapEntryModel(Entry<K, V> entry) {
        this.entry = entry;
    }

    @Override
    public Entry<K, V> getObject() {
        return entry;
    }

    @Override
    public void setObject(Entry<K, V> object) {
        entry = object;
    }

    @Override
    public void detach() {
    }

}
