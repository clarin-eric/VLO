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
 * Model that represents a 'bridge' between two models that can be opened and
 * closed; one model is the inner model, the value of which is used by some
 * service or model; the other is the outer model, which is the model the user
 * can manipulate most directly
 *
 * @author twagoo
 * @param <T> type of the inner and outer model
 * @see BridgeOuterModel
 */
public class BridgeModel<T extends Serializable> implements IModel<Boolean> {

    private final IModel<T> innerModel;
    private final IModel<T> outerModel;
    private final IModel<Boolean> bridgeStateModel;
    private final T falseValue;

    /**
     *
     * @param innerModel inner, target model that holds the core value
     * @param outerModel outer model, with which the user interacts (should be a
     * {@link BridgeOuterModel} for the whole construct to work properly)
     * @param bridgeStateModel model that indicates whether the 'bridge' is open
     * @param falseValue value to be set on the target when the 'bridge' closes
     */
    public BridgeModel(IModel<T> innerModel, IModel<T> outerModel, IModel<Boolean> bridgeStateModel, T falseValue) {
        this.innerModel = innerModel;
        this.outerModel = outerModel;
        this.bridgeStateModel = bridgeStateModel;
        this.falseValue = falseValue;
    }

    @Override
    public Boolean getObject() {
        return bridgeStateModel.getObject();
    }

    @Override
    public void setObject(Boolean object) {
        if (object) {
            innerModel.setObject(outerModel.getObject());
        } else {
            innerModel.setObject(falseValue);
        }
        bridgeStateModel.setObject(object);
    }

    @Override
    public void detach() {
        innerModel.detach();
        outerModel.detach();
        bridgeStateModel.detach();
    }

}
