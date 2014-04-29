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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

/**
 * A behavior that hides all DOM elements with class <em>nonjsfallback</em>
 * through a JavaScript call when the dom is ready. Obviously, this has no
 * effect when JavaScript is not enabled, leaving these fallback controls
 * available.
 *
 * @author twagoo
 * @see OnDomReadyHeaderItem
 */
public class HideJavascriptFallbackControlsBehavior extends Behavior {

    public static final String HIDE_JAVASCRIPT_FALLBACK_SCRIPT = "var sheet = document.createElement('style')\n"
            + "sheet.innerHTML = \".nonjsfallback {display: none;}\";\n"
            + "document.body.appendChild(sheet);";

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        // execute javascript that adds a CSS rule to hide all .nonjsfallback elements
        response.render(OnDomReadyHeaderItem.forScript(HIDE_JAVASCRIPT_FALLBACK_SCRIPT));
    }
}
