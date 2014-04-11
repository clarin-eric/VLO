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
 *
 * @author twagoo
 * @param <T> type of the model, should match the inner model
 * @see BridgeModel
 */
public class BridgeOuterModel<T extends Serializable> implements IModel<T> {
    
    private final IModel<T> innerModel;
    private final IModel<Boolean> bridgeStateModel;
    private T value;

    /**
     *
     * @param innerModel model to be bridged
     * @param bridgeStateModel model that indicates whether the 'bridge' is open
     * @param value initial value of the outer model
     */
    public BridgeOuterModel(IModel<T> innerModel, IModel<Boolean> bridgeStateModel, T value) {
        this.innerModel = innerModel;
        this.bridgeStateModel = bridgeStateModel;
        this.value = value;
        
        if (bridgeStateModel.getObject()) {
            innerModel.setObject(value);
        }
    }
    
    @Override
    public T getObject() {
        return value;
    }
    
    @Override
    public void setObject(T object) {
        value = object;
        if (bridgeStateModel.getObject()) {
            innerModel.setObject(object);
        }
    }
    
    @Override
    public void detach() {
        innerModel.detach();
        bridgeStateModel.detach();
    }
    
}
