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
import org.apache.wicket.model.IModel;

/**
 * Wraps a model and allows for toggling between two values as the model object
 * of the wrapped model by calling ({@link #setObject(java.lang.Boolean)
 * }) on <em>this</em>, which will result in a setting of either the 'false
 * value' object or the 'true value'.
 *
 * @author twagoo
 * @param <T> type for values and wrapped model
 */
public class ToggleModel<T extends Serializable> implements IModel<Boolean> {

    private final IModel<T> wrappedModel;
    private final T falseValue;
    private final T trueValue;

    /**
     *
     * @param wrappedModel model to wrap
     * @param falseValue value to set on the wrapped model if <em>this</em> is
     * set to false (can be null)
     * @param trueValue value to set on the wrapped model if <em>this</em> is
     * set to true (should not be null)
     */
    public ToggleModel(IModel<T> wrappedModel, T falseValue, T trueValue) {
        this.wrappedModel = wrappedModel;
        this.falseValue = falseValue;
        this.trueValue = trueValue;
    }

    /**
     *
     * @return whether {@link IModel#getObject() } of the wrapped model is equal
     * to the 'true value' object
     */
    @Override
    public Boolean getObject() {
        return trueValue.equals(wrappedModel.getObject());
    }

    /**
     *
     * @param object sets the value of the wrapped model to the 'true value'
     * object if object equals {@link Boolean#TRUE}, otherwise to the 'false
     * value' object
     */
    @Override
    public void setObject(Boolean object) {
        if (object) {
            wrappedModel.setObject(trueValue);
        } else {
            wrappedModel.setObject(falseValue);
        }
    }

    /**
     * Detaches the wrapped model
     */
    @Override
    public void detach() {
        wrappedModel.detach();
    }

}
