/*
 * Copyright (C) 2021 CLARIN
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

import java.util.Optional;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class UnwrappedOptionalModel<T> implements IModel<T> {
    
    private final IModel<Optional<T>> wrappedModel;
    private final IModel<T> orElseModel;

    public UnwrappedOptionalModel(IModel<Optional<T>> wrapped) {
        this(wrapped, new Model(null));
    }

    public UnwrappedOptionalModel(IModel<Optional<T>> wrappedModel, IModel<T> orElseModel) {
        this.wrappedModel = wrappedModel;
        this.orElseModel = orElseModel;
    }

    @Override
    public T getObject() {
        return wrappedModel.getObject().orElse(orElseModel.getObject());
    }

    @Override
    public void setObject(T object) {
        wrappedModel.setObject(Optional.ofNullable(object));
    }

    @Override
    public void detach() {
        wrappedModel.detach();
    }
    
}
