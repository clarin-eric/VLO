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

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.settings.def.ResourceSettings;

/**
 * Label that uses a {@link StringResourceModel} to display a human friendly
 * and/or localised name instead of the internal field name. This depends on the
 * presence of a globally registered resource bundle that contains a property
 * "field.{fieldname}".
 *
 * @author twagoo
 * @see Application#getResourceSettings() 
 * @see ResourceSettings#getStringResourceLoaders() 
 */
public class SolrFieldNameLabel extends Label {

    public SolrFieldNameLabel(String id, IModel<String> model) {
        super(id,
                new StringResourceModel(
                        "field.${}", // property to get name fram
                        model, // model holds field name
                        model.getObject())); // default to internal field name
    }

}
