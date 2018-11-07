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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDLinkLabel extends Panel {

    private final PIDLabel pidLabel;

    /**
     *
     * @param id component id
     * @param model link model
     */
    public PIDLinkLabel(String id, IModel<String> model) {
        this(id, model, -1);
    }

    /**
     *
     * @param id component id
     * @param model link model
     * @param maxLinkLength maximum length before text inside label gets
     * truncated
     */
    public PIDLinkLabel(String id, IModel<String> model, int maxLinkLength) {
        super(id);

        this.pidLabel = new PIDLabel("label", model, maxLinkLength);
        add(new ExternalLink("link", PIDLinkModel.wrapLinkModel(model))
                .add(pidLabel)
                .add(new AttributeModifier("title", model)));
    }

    public PIDLinkLabel setHideLabel(boolean hideLabel) {
        pidLabel.setHideLabel(hideLabel);
        return this;
    }
}
