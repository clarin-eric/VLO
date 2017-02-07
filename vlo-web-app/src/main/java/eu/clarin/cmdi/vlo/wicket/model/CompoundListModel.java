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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * List model that wraps a list of models
 *
 * @author twagoo
 * @param <T> model type
 */
public class CompoundListModel<T> extends AbstractReadOnlyModel<List<T>> {

    private final List<IModel<T>> modelsList;

    /**
     *
     * @param model models to provide as a list of objects
     */
    public CompoundListModel(IModel<T>... model) {
        this(Arrays.asList(model));
    }

    /**
     *
     * @param models list of models to provide as a list of objects
     */
    public CompoundListModel(List<IModel<T>> models) {
        this.modelsList = models;
    }

    @Override
    public List<T> getObject() {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return modelsList.get(index).getObject();
            }

            @Override
            public int size() {
                return modelsList.size();
            }
        };
    }

    @Override
    public void detach() {
        for (IModel<T> model : modelsList) {
            model.detach();
        }
    }

}
