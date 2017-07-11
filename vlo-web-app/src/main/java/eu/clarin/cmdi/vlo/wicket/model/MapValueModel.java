/*
 * Copyright (C) 2016 CLARIN
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
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class MapValueModel<K, V> implements IModel<V> {

    private final IModel<Map<K, V>> mapModel;
    private final IModel<K> keyModel;

    public MapValueModel(IModel<Map<K, V>> mapModel, IModel<K> keyModel) {
        this.mapModel = mapModel;
        this.keyModel = keyModel;
    }

    @Override
    public V getObject() {
        return mapModel.getObject().get(keyModel.getObject());
    }

    @Override
    public void setObject(V object) {
        mapModel.getObject().put(keyModel.getObject(), object);
    }

    @Override
    public void detach() {
        mapModel.detach();
        keyModel.detach();
    }

}
