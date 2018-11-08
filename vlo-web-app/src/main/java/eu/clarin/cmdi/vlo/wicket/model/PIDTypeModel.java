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

import eu.clarin.cmdi.vlo.PIDType;
import eu.clarin.cmdi.vlo.PIDUtils;
import java.util.Optional;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDTypeModel extends LoadableDetachableModel<PIDType> {

    private final IModel<String> linkModel;

    public PIDTypeModel(IModel<String> linkModel) {
        this.linkModel = linkModel;
    }

    @Override
    protected PIDType load() {
        final Optional<PIDType> type = PIDUtils.getType(linkModel.getObject());
        return type.orElse(null);
    }

    @Override
    public void detach() {
        super.detach();
        linkModel.detach();
    }

}
