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
package eu.clarin.cmdi.vlo.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class BooleanVisibilityBehavior extends Behavior {
    
    private final IModel<Boolean> model;
    private final Boolean visibilityState;

    private BooleanVisibilityBehavior(IModel<Boolean> model, Boolean visibilityState) {
        this.model = model;
        this.visibilityState = visibilityState;
    }

    @Override
    public void onConfigure(Component component) {
        component.setVisible(visibilityState.equals(model.getObject()));
    }

    public static BooleanVisibilityBehavior visibleOnTrue(IModel<Boolean> model) {
        return new BooleanVisibilityBehavior(model, Boolean.TRUE);
    }

    public static BooleanVisibilityBehavior visibleOnFalse(IModel<Boolean> model) {
        return new BooleanVisibilityBehavior(model, Boolean.FALSE);
    }
    
}
