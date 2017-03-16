/*
 * Copyright (C) 2017 CLARIN
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

import com.google.common.collect.Ordering;
import java.util.List;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 * @param <T>
 */
public class OrderedListModel<T> implements IModel<List<T>> {

    private final IModel<List<T>> listModel;
    private final IModel<Ordering<T>> orderingModel;

    public OrderedListModel(IModel<List<T>> listModel, IModel<Ordering<T>> orderingModel) {
        this.listModel = listModel;
        this.orderingModel = orderingModel;
    }

    @Override
    public List<T> getObject() {
        final Ordering<T> ordering = orderingModel.getObject();
        
        if (ordering == null) {
            return listModel.getObject();
        } else {
            return ordering.immutableSortedCopy(listModel.getObject());
        }
    }

    @Override
    public void setObject(List<T> object) {
        listModel.setObject(object);
    }

    @Override
    public void detach() {
        listModel.detach();
    }

}
