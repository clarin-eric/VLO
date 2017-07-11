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
package eu.clarin.cmdi.vlo.wicket.panels;

import eu.clarin.cmdi.vlo.wicket.model.BooleanOptionsModel;
import java.io.Serializable;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Bootstrap drop down menu component that also works without javascript
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class BootstrapDropdown extends GenericPanel<List<BootstrapDropdown.DropdownMenuItem>> {

    private final IModel<Boolean> openStateModel;

    public BootstrapDropdown(String id, IModel<List<DropdownMenuItem>> itemsModel) {
        super(id, itemsModel);
        this.openStateModel = Model.of(false);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new AttributeAppender("class", new BooleanOptionsModel(openStateModel, Model.of("dropdown open"), Model.of("dropdown")), " "));

        add(createDropDownLink("button"));
        add(createMenu("menu"));
    }

    protected Component createDropDownLink(String id) {
        //link that activates dropdown
        final Link<Boolean> link = new Link<Boolean>(id, openStateModel) {
            @Override
            public void onClick() {
                getModel().setObject(!getModelObject());
            }
        };
        final Serializable buttonClass = getButtonClass();
        if (buttonClass != null) {
            link.add(new AttributeAppender("class", buttonClass, " "));
        }

        //optional icon
        final WebMarkupContainer icon = new WebMarkupContainer("buttonIcon");
        final Serializable iconClass = getButtonIconClass();
        if (iconClass != null) {
            icon.add(new AttributeModifier("class", iconClass));
        }
        link.add(icon);

        //caret
        final WebMarkupContainer caret = new WebMarkupContainer("caret");
        caret.setVisible(showCaret());
        link.add(caret);
        return link;
    }

    /**
     *
     * @return "btn btn-default" in default implementation
     */
    protected Serializable getButtonClass() {
        return "btn btn-default";
    }

    protected Serializable getButtonIconClass() {
        return null;
    }

    protected boolean showCaret() {
        return true;
    }

    protected Component createMenu(String id) {
        final WebMarkupContainer menu = new WebMarkupContainer(id);
        menu.add(new ListView<DropdownMenuItem>("menuItem", getModel()) {
            @Override
            protected void populateItem(ListItem<DropdownMenuItem> item) {
                final DropdownMenuItem itemObject = item.getModelObject();
                //link that forms the menu item's action
                item.add(itemObject.getLink("link")
                        //link label defined by the menu item object
                        .add(new Label("label", itemObject.getLabel()))
                        //icon with icon class defined by the menu item object
                        .add(new WebMarkupContainer("icon")
                                .add(new AttributeModifier("class", itemObject.getIconClass())))
                );
            }
        });
        return menu;
    }

    public abstract static class DropdownMenuItem implements Serializable {

        private final String label;
        private final String iconClass;

        public DropdownMenuItem(String label, String iconClass) {
            this.label = label;
            this.iconClass = iconClass;
        }

        public String getLabel() {
            return label;
        }

        public String getIconClass() {
            return iconClass;
        }

        protected abstract Link getLink(String id);
    }

    public void close() {
        openStateModel.setObject(false);
    }

}
