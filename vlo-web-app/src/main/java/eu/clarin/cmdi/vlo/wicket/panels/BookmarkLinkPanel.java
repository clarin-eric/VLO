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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class BookmarkLinkPanel extends Panel {

    private final IModel<String> linkModel;
    private final IModel<String> pageTitleModel;

    public BookmarkLinkPanel(String id, IModel<String> linkModel, IModel<String> pageTitleModel) {
        super(id);
        this.linkModel = linkModel;
        this.pageTitleModel = pageTitleModel;

        add(new ExternalLink("link", linkModel)
                .add(new Label("linkText", pageTitleModel)));
        add(new TextField("linkInput", linkModel));
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (linkModel != null) {
            linkModel.detach();
        }
        if (pageTitleModel != null) {
            pageTitleModel.detach();
        }
    }

}
