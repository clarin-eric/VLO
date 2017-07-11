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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public abstract class ToggleLink extends GenericPanel<Boolean> {

    public ToggleLink(String id, IModel<Boolean> model, final IModel<String> showTextModel, final IModel<String> hideTextModel) {
        super(id, model);
        final Link<Boolean> link = new IndicatingAjaxFallbackLink<Boolean>("link", model) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                getModel().setObject(!getModelObject());
                if (target != null) {
                    target.add(ToggleLink.this);
                }
                ToggleLink.this.onClick(target);
            }
        };
        link.add(new Label("label", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (ToggleLink.this.getModelObject()) {
                    return hideTextModel.getObject();
                } else {
                    return showTextModel.getObject();
                }
            }

            @Override
            public void detach() {
                showTextModel.detach();
                hideTextModel.detach();
            }

        }));
        add(link);

        setOutputMarkupId(true);
    }

    protected abstract void onClick(AjaxRequestTarget target);

}
