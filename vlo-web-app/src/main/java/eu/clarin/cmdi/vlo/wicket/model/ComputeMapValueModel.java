/*
 * Copyright (C) 2020 CLARIN
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

import java.util.Map;
import java.util.Optional;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public abstract class ComputeMapValueModel<K, V> extends MapValueModel<K, V> {

    public ComputeMapValueModel(IModel<? extends Map<K, V>> mapModel, IModel<K> keyModel) {
        super(mapModel, keyModel);
    }

    @Override
    public V getObject() {
        final Optional<V> object = Optional.ofNullable(super.getObject());
        return object.orElseGet(() -> {
            final V computed = computeObject(getKeyModel());
            setObject(computed);
            return computed;
        });
    }

    protected abstract V computeObject(IModel<K> keyModel);

}
