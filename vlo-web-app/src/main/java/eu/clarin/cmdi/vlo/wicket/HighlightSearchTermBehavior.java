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
package eu.clarin.cmdi.vlo.wicket;

import eu.clarin.cmdi.vlo.JavaScriptResources;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

/**
 * A behavior that adds JavaScript, executed on DOM ready, to highlight
 * occurrences of the current searchword (retrieved from the 'q' query parameter
 * by the client) in the page
 *
 * @author twagoo
 */
public class HighlightSearchTermBehavior extends Behavior {

    private static final String HIGHLIGHT_FUNCTION = "searchhi.init()";

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        // include highlight script
        response.render(JavaScriptHeaderItem.forReference(JavaScriptResources.getHighlightJS()));
        // after load, highlight 
        response.render(OnDomReadyHeaderItem.forScript(HIGHLIGHT_FUNCTION));
    }

}
