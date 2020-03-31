/*
 * Copyright (C) 2020 CLARIN
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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public abstract class JsonLdHeaderBehavior extends Behavior {

    private final IModel<String> jsonLdContentModel;

    protected JsonLdHeaderBehavior(IModel<String> jsonLdContentModel) {
        this.jsonLdContentModel = jsonLdContentModel;
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        final String script = "<script type=\"application/ld+json\">\n"
                + "/*<![CDATA[*/\n"
                + jsonLdContentModel.getObject()
                + "/*]]>*/\n"
                + "</script>\n";
        response.render(new StringHeaderItem(script));
    }

}
