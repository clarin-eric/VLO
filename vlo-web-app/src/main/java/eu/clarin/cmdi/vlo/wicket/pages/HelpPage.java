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

import eu.clarin.cmdi.vlo.JavaScriptResources;
import eu.clarin.cmdi.vlo.config.VloConfig;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Mostly markup, with table of contents (using bootstrap-toc)
 *
 * @author twagoo
 * @see https://afeld.github.io/bootstrap-toc/
 */
public class HelpPage extends VloBasePage {

    @SpringBean
    private VloConfig vloConfig;

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new ExternalLink("moreInfo", vloConfig.getHelpUrl()));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        // Table of content with scrollspy (required CSS is compiled along with the VLO LESS)
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getBootstrapToc()));
        // Enable scroll spy on the body tag, required for the table of contents
        final String scrollspyScript = ""
                + "$(document).ready(function() {"
                + "  $('body').scrollspy({ target: '#toc' });"
                + "});";
        response.render(JavaScriptHeaderItem.forScript(scrollspyScript, "scrollspy"));
    }

}
