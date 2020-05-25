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
package eu.clarin.cmdi.vlo.wicket.components;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.markup.html.basic.Label;
import java.util.MissingResourceException;


public class ResourceTypeLabel extends Label {
    //private final IModel<String> labelModel;

    public ResourceTypeLabel(String id, IModel<String> mimeTypeModel) {
        super(id, new StringResourceModel("mimetype.${}.label", normalizeMimeType(mimeTypeModel))
                .setDefaultValue(mimeTypeModel.getObject()));
    }


    private static IModel<String> normalizeMimeType(IModel<String> mimeType) {
        String type = mimeType.getObject();
        System.out.println(type);
        if (type != null) {
            type = type.toLowerCase();
            type = type.replaceAll(" ", "_");
            type = type.replaceAll("/", "_");
            type = type.replaceAll("-", "_");
            type = type.replaceAll("\\+", "_");
            type = type.replaceAll("\\.", "_");
        } else {
            type = "landing_page";
        }
        return Model.of(type);
    }

}
