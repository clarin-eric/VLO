/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.wicket.HideJavascriptFallbackControlsBehavior;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

/**
 *
 * @author twagoo
 */
public class VloBasePage<T> extends GenericWebPage<T> {

    @SpringBean
    private VloConfig vloConfig;

    public VloBasePage() {
        addComponents();
    }

    public VloBasePage(IModel<T> model) {
        super(model);
        addComponents();
    }

    public VloBasePage(PageParameters parameters) {
        super(parameters);
        processTheme(parameters);
        addComponents();
    }

    private void processTheme(PageParameters parameters) {
        final StringValue theme = parameters.get(VloWebAppParameters.THEME);
        if (!theme.isNull()) {
            if (theme.isEmpty()) {
                Session.get().setStyle(null);
            } else {
                Session.get().setStyle(theme.toString().toLowerCase());
            }
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        // Include CSS. Exact file will be chosen on basis of current locale and
        // style (theme).
        response.render(CssHeaderItem.forReference(new CssResourceReference(VloBasePage.class, "vlo.css", getLocale(), getStyle(), getVariation())));
    }

    private void addComponents() {
        add(new FeedbackPanel("feedback"));
        add(new ExternalLink("help", vloConfig.getHelpUrl()));

        add(new HideJavascriptFallbackControlsBehavior());
    }


}
