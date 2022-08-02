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

import eu.clarin.cmdi.vlo.PIDUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author CLARIN ERIC <clarin@clarin.eu>
 */
public class ActionableLinkModel extends Model<String> {

    private final IModel<String> wrappedModel;

    public ActionableLinkModel(IModel<String> wrappedModel) {
        this.wrappedModel = wrappedModel;
    }

    @Override
    public String getObject() {
        final String wrappedObject = wrappedModel.getObject();
        if (wrappedObject == null) {
            return null;
        } else {
            if (PIDUtils.isPid(wrappedObject)) {
                return PIDUtils.getActionableLinkForPid(wrappedObject);
            } else if (PIDUtils.isActionableLink(wrappedObject)) {
                return wrappedObject;
            } else {
                return null;
            }
        }
    }

    @Override
    public void detach() {
        super.detach();
        wrappedModel.detach();
    }

}
