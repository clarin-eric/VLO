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
package eu.clarin.cmdi.vlo.wicket.components;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import eu.clarin.cmdi.vlo.PIDUtils;

/**
 * Panel that shows an icon to mark the 'PID' nature of the link represented by
 * the model. Not visible unless link is actually a PID.
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see PIDUtils#isPid(java.lang.String)
 */
public class PIDBadge extends GenericPanel<String> {

    /**
     *
     * @param id
     * @param model model of link for which to check whether it is a pid
     */
    public PIDBadge(String id, IModel<String> model) {
        super(id, model);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(PIDUtils.isPid(getModelObject()));
    }

}
