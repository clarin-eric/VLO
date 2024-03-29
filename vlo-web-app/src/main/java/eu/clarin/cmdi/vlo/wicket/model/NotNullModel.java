/*
 * Copyright (C) 2022 CLARIN
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
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class NotNullModel<T> implements IModel<Boolean> {

    private final IModel<T> wrappedModel;

    public NotNullModel(IModel<T> wrappedModel) {
        this.wrappedModel = wrappedModel;
    }

    @Override
    public Boolean getObject() {
        return wrappedModel != null && wrappedModel.getObject() != null;
    }

    @Override
    public void detach() {
        wrappedModel.detach();
    }

    public final static <T> IModel<Boolean> of(IModel<T> wrappedModel) {
        return new NotNullModel(wrappedModel);
    }

}
