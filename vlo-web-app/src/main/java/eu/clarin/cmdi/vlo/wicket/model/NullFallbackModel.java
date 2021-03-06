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

import java.io.Serializable;
import java.util.MissingResourceException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Decorator for String models that replaces the value returned by 
 * {@link IModel#getObject() } in the wrapped model by a fallback value if it's
 * null
 *
 * @author twagoo
 * @param <T> Model type
 */
public class NullFallbackModel<T extends Serializable> implements IModel<T> {

    private final IModel<T> wrappedModel;
    private final IModel<T> fallbackModel;

    public NullFallbackModel(IModel<T> wrappedModel, T fallbackValue) {
        this(wrappedModel, Model.of(fallbackValue));
    }

    public NullFallbackModel(IModel<T> wrappedModel, IModel<T> fallbackModel) {
        this.wrappedModel = wrappedModel;
        this.fallbackModel = fallbackModel;
    }

    @Override
    public T getObject() {
        try {
            final T wrappedValue = wrappedModel.getObject();
            if (wrappedValue == null) {
                return fallbackModel.getObject();
            } else {
                return wrappedValue;
            }
        } catch (MissingResourceException ex) {
            //this can happen with the StringResourceModel if the property is not defined
            return fallbackModel.getObject();
        }
    }

    @Override
    public void setObject(T object) {
        wrappedModel.setObject(object);
    }

    @Override
    public void detach() {
        wrappedModel.detach();
        fallbackModel.detach();
    }

}
