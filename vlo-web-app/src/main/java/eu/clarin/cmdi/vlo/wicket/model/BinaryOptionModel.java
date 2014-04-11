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

import org.apache.wicket.model.IModel;

/**
 * Wrapper model that maps between two distinct values (provided by models) and
 * a boolean
 *
 * @author twagoo
 */
public class BinaryOptionModel<T> implements IModel<Boolean> {

    private final IModel<T> wrappedModel;
    private final IModel<T> falseModel;
    private final IModel<T> trueModel;

    public BinaryOptionModel(IModel<T> wrappedModel, IModel<T> falseValue, IModel<T> trueValue) {
        this.wrappedModel = wrappedModel;
        this.falseModel = falseValue;
        this.trueModel = trueValue;
    }

    @Override
    public Boolean getObject() {
        return wrappedModel.getObject().equals(trueModel.getObject());
    }

    @Override
    public void setObject(Boolean object) {
        if (object) {
            wrappedModel.setObject(trueModel.getObject());
        } else {
            wrappedModel.setObject(falseModel.getObject());
        }
    }

    @Override
    public void detach() {
    }

}
