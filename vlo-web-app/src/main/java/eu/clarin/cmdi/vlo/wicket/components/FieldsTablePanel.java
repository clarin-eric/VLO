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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.pojo.DocumentField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author twagoo
 */
public class FieldsTablePanel extends Panel {

    public FieldsTablePanel(String id, IDataProvider<DocumentField> fieldProvider) {
        super(id);
        add(new DataView<DocumentField>("documentField", fieldProvider) {

            @Override
            protected void populateItem(Item<DocumentField> item) {
                final IModel<DocumentField> fieldModel = item.getModel();
                item.add(new Label("fieldName", new PropertyModel(fieldModel, "fieldName")));
                item.add(new ListView("values", new PropertyModel(fieldModel, "values")) {

                    @Override
                    protected void populateItem(ListItem item) {
                        item.add(new Label("value", item.getModel()));
                    }
                });
            }
        });
    }

}
