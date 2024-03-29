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

/**
 * Attach to a &lt;span&gt; element
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class ResourceTypeIcon extends WebMarkupContainer {

    private final IModel<String> iconModel;

    public final static String LANDING_PAGE = "LANDINGPAGE";
    public final static String SEARCH_PAGE = "SEARCHPAGE";
    public final static String SEARCH_SERVICE = "SEARCHSERVICE";
    public final static String HIERARCHY = "HIERARCHY";

    public ResourceTypeIcon(String id, IModel<String> model) {
        super(id, model);

        iconModel = new StringResourceModel("resourcetype.${}.fonticonclass", model)
                .setDefaultValue("glyphicon-question-sign");
        add(new AttributeAppender("class", "glyphicon", " "));
        add(new AttributeAppender("class", iconModel, " "));
    }

}
