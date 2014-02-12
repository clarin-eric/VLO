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
package eu.clarin.cmdi.vlo.components;

import eu.clarin.cmdi.vlo.pojo.FacetStatus;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.util.ListModel;

/**
 *
 * @author twagoo
 */
public class FacetsPanel extends Panel {

    public FacetsPanel(String id, ListModel<FacetStatus> model) {
        super(id, model);
        add(new ListView<FacetStatus>("facets", model) {

            @Override
            protected void populateItem(ListItem<FacetStatus> item) {
                //TODO: Check whether a value has been selected or not
                item.add(new FacetPanel("facet", item.getModel()));
            }
        });
    }

}
