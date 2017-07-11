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
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.IModel;

/**
 * Attach to a &lt;span&gt; element
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class ResourceTypeIcon extends WebMarkupContainer {

    private final IModel<String> iconModel;

    public ResourceTypeIcon(String id, IModel<String> model) {
        super(id, model);

        iconModel = StringResourceModelMigration.of("resourcetype.${}.fonticonclass", model, "glyphicon-question-sign");
        add(new AttributeAppender("class", "glyphicon", " "));
        add(new AttributeAppender("class", iconModel, " "));
    }

}
