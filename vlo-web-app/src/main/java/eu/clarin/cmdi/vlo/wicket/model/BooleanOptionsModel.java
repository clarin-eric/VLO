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

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class BooleanOptionsModel<T> extends AbstractReadOnlyModel<T> {

    private final IModel<Boolean> innerModel;
    private final IModel<T> trueValue;
    private final IModel<T> falseValue;

    public BooleanOptionsModel(IModel<Boolean> innerModel, IModel<T> trueValue, IModel<T> falseValue) {
        this.innerModel = innerModel;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
    }

    @Override
    public T getObject() {
        final Boolean object = innerModel.getObject();
        if (object == null) {
            return null;
        } else if (object) {
            return trueValue.getObject();
        } else {
            return falseValue.getObject();
        }
    }

}
