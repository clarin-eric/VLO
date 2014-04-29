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
 * Decorator for String models that replaces the value returned by 
 * {@link IModel#getObject() } in the wrapped model by a fallback value if it's
 * null
 *
 * @author twagoo
 */
public class NullFallbackModel implements IModel<String> {

    private final IModel<String> wrappedModel;
    private final String fallbackValue;

    public NullFallbackModel(IModel<String> wrappedModel, String fallbackValue) {
        this.wrappedModel = wrappedModel;
        this.fallbackValue = fallbackValue;
    }

    @Override
    public String getObject() {
        final String wrappedValue = wrappedModel.getObject();
        if (wrappedValue == null) {
            return fallbackValue;
        } else {
            return wrappedValue;
        }
    }

    @Override
    public void setObject(String object) {
        wrappedModel.setObject(object);
    }

    @Override
    public void detach() {
        wrappedModel.detach();
    }

}
