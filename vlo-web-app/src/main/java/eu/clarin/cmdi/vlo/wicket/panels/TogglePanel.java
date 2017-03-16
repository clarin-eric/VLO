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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.wicket.components.ToggleLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A panel that wraps a content component and adds a link to toggle its
 * visibility. The toggle link is an Ajax fallback link with a dynamic label,
 * showing a custom text depending on the toggle state.
 *
 * @author twagoo
 */
public abstract class TogglePanel extends Panel {

    private final IModel<Boolean> visibilityModel = Model.of(false);
    private final Component content;

    /**
     *
     * @param id component id
     * @param showTextModel model for text to show when collapsed ('show
     * content')
     * @param hideTextModel model for text to show when expanded ('hide
     * content')
     */
    public TogglePanel(String id, final IModel<String> showTextModel, final IModel<String> hideTextModel) {
        super(id);

        // add the actual toggle link
        final Component toggler = new ToggleLink("toggler", visibilityModel, showTextModel, hideTextModel) {
            @Override
            protected void onClick(AjaxRequestTarget target) {
                if (target != null) {
                    target.add(TogglePanel.this);
                }
            }
        };
        add(toggler);

        // make 'class' attribute depend on toggle state
        add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                if (visibilityModel.getObject()) {
                    return getExpandedClass();
                } else {
                    return getCollapsedClass();
                }
            }
        }));

        // add the toggled content
        content = createContent("toggleContent");
        add(content);

        setOutputMarkupId(true);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        content.setVisible(visibilityModel.getObject());
    }

    /**
     *
     * @return class set on the panel markup element when toggle state is
     * collapsed (this implementation returns "collapsed")
     */
    protected static String getCollapsedClass() {
        return "collapsed";
    }

    /**
     *
     * @return class set on the panel markup element when toggle state is
     * expanded (this implementation returns "expanded")
     */
    protected static String getExpandedClass() {
        return "expanded";
    }

    /**
     * Implementer should return content component in this method!
     *
     * @param id component id to use for content
     * @return component that will be rendered as toggled content
     */
    protected abstract Component createContent(String id);

}
