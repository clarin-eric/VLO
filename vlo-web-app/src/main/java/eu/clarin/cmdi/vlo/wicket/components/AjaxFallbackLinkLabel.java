/*
 * Copyright (C) 2015 CLARIN
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
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public abstract class AjaxFallbackLinkLabel<T> extends Panel {

    public AjaxFallbackLinkLabel(String id, IModel<T> model, IModel<?> contentModel) {
        this(id, model, contentModel, true);
    }

    public AjaxFallbackLinkLabel(String id, IModel<T> linkModel, IModel<?> contentModel, boolean indicator) {
        super(id);
        final Link link;
        if (indicator) {
            link = new IndicatingAjaxFallbackLink<T>("link", linkModel) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    AjaxFallbackLinkLabel.this.onClick(target);
                }
            };
        } else {
            link = new AjaxFallbackLink<T>("link", linkModel) {

                @Override
                public void onClick(AjaxRequestTarget target) {
                    AjaxFallbackLinkLabel.this.onClick(target);
                }
            };
        }
        add(link);

        link.add(new Label("content", contentModel));
    }

    protected abstract void onClick(AjaxRequestTarget target);

}
