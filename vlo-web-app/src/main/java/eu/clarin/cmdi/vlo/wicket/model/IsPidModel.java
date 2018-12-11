/*
 * Copyright (C) 2018 CLARIN
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

import eu.clarin.cmdi.vlo.PIDUtils;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class IsPidModel extends AbstractReadOnlyModel<Boolean> {

    private final IModel<String> uriModel;

    public IsPidModel(IModel<String> uriModel) {
        this.uriModel = uriModel;
    }

    @Override
    public Boolean getObject() {
        return PIDUtils.isPid(uriModel.getObject());
    }

    @Override
    public void detach() {
        uriModel.detach();
    }

}
