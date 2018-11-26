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

import eu.clarin.cmdi.vlo.wicket.model.PIDContext;
import eu.clarin.cmdi.vlo.wicket.model.PIDLinkModel;
import eu.clarin.cmdi.vlo.wicket.panels.BootstrapModal;
import eu.clarin.cmdi.vlo.wicket.panels.PIDInfoPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class PIDLinkLabel extends GenericPanel<String> {

    private final PIDLabel pidLabel;
    private final BootstrapModal pidInfoModal;

    /**
     *
     * @param id component id
     * @param model link model
     */
    public PIDLinkLabel(String id, IModel<String> model, IModel<PIDContext> pidContextModel) {
        this(id, model, pidContextModel, -1);
    }

    /**
     *
     * @param id component id
     * @param model link model
     * @param maxLinkLength maximum length before text inside label gets
     * truncated
     */
    public PIDLinkLabel(String id, IModel<String> model, IModel<PIDContext> pidContextModel, int maxLinkLength) {
        super(id, model);

        this.pidLabel = new PIDLabel("label", model, maxLinkLength);
        final Link link = new IndicatingAjaxFallbackLink("link", PIDLinkModel.wrapLinkModel(model)) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                PIDLinkLabel.this.onClick(target);
            }

        };
        add(link
                .add(pidLabel)
                .add(new AttributeModifier("title", model)));

        // Modal dialogue with PID info
        pidInfoModal = new BootstrapModal("pidInfo") {
            @Override
            protected IModel<String> getTitle() {
                return Model.of("Persistent identifier info");
            }
        };
        pidInfoModal.add(new PIDInfoPanel(pidInfoModal.getContentId(), getModel(), pidContextModel));
        add(pidInfoModal);
    }

    protected void onClick(AjaxRequestTarget target) {
        if (target != null) {
            pidInfoModal.show(target);
        } else {
            //no JS - redirect to PID (resolver) URL
            throw new RedirectToUrlException(PIDLinkModel.wrapLinkModel(getModel()).getObject());
        }
    }

    public PIDLinkLabel setHideLabel(boolean hideLabel) {
        pidLabel.setHideLabel(hideLabel);
        return this;
    }
}
