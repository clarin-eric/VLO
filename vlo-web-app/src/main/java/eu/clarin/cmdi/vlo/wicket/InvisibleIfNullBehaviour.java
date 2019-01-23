/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.vlo.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;

/**
 * Sets component visibility depending on null state of model object:
 * 
 * {@code component.setVisible(model.getObject() != null);}
 * 
 * @author Twan Goosen <twan@clarin.eu>
 */
public class InvisibleIfNullBehaviour<T> extends Behavior {

    private final IModel<T> model;

    public InvisibleIfNullBehaviour(IModel<T> checkModel) {
        this.model = checkModel;
    }

    @Override
    public void onConfigure(Component component) {
        component.setVisible(model.getObject() != null);
    }

}
