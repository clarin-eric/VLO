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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Behaviour that adds the "disabled" class to a markup element and sets the
 * "disabled" attribute to "disabled" iff a (provided) check indicates that the
 * component is not enabled. Useful with bootstrap link buttons.
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class LinkDisabledClassBehaviour extends Behavior {

    @Override
    public void bind(Component component) {
        super.bind(component);
        if (component instanceof Link) {
            final Link link = (Link) component;
            final AbstractReadOnlyModel<String> disabledModel = new AbstractReadOnlyModel<String>() {
                @Override
                public String getObject() {
                    return link.isEnabled() ? null : "disabled";
                }
            };
            link.add(new AttributeAppender("class", disabledModel, " "));
            link.add(new AttributeModifier("disabled", disabledModel));
        } else {
            throw new RuntimeException(getClass().getName() + " can only be used with Link components");
        }
    }
}
