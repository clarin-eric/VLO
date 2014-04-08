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

import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.wicket.markup.html.GenericWebPage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

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
        addComponents();
    }

    private void addComponents() {
        add(new FeedbackPanel("feedback"));
        add(new ExternalLink("help", vloConfig.getHelpUrl()));
    }

}
