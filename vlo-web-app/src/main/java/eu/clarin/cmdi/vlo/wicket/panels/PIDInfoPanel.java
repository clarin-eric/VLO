/*
 * Copyright (C) 2018 CLARIN
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

import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDInfoPanel extends GenericPanel<String> {

    //TODO: resolving link
    //TODO: PID type
    //TODO: copy to clipboard
    //TODO: "what is a pid?" content
    
    public PIDInfoPanel(String id, IModel<String> model) {
        super(id, PIDLinkModel.wrapLinkModel(model));

        final IModel<String> pidLinkModel = getModel();

        add(new TextField("pidInputField", pidLinkModel));
        add(new ExternalLink("pidLink", pidLinkModel));
    }

}
