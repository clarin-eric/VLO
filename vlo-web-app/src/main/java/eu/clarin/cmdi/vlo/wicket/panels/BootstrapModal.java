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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

/**
 * Drop-in replacement for {@link ModalWindow} that uses bootstrap classes and
 * JavaScript.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class BootstrapModal extends Panel {

    private final IModel<Boolean> visibilityModel = Model.of(false);

    public BootstrapModal(String id) {
        super(id);
    }

    public BootstrapModal(String id, IModel<?> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("title", getTitle()));
        setOutputMarkupId(true);
    }

    @Override
    protected void onConfigure() {
        get("title").setVisible(visibilityModel.getObject());
        get("bodyContent").setVisible(visibilityModel.getObject());
    }

    protected abstract IModel<String> getTitle();

    public void close(AjaxRequestTarget target) {
        visibilityModel.setObject(false);
        target.add(this);
        target.prependJavaScript(String.format("cb|hideModal($('#%s .modal'), cb);", getMarkupId(true)));
    }

    public void show(AjaxRequestTarget target) {
        visibilityModel.setObject(true);
        target.add(this);
        target.appendJavaScript(String.format("showModal($('#%s .modal'));", getMarkupId(true)));
    }
    
    public void show() {
        visibilityModel.setObject(true);
    }

    public String getContentId() {
        return "bodyContent";
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(BootstrapModal.class, "bootstrap-modal.js")));
    }

}
