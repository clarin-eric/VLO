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
package eu.clarin.cmdi.vlo.wicket.historyapi;

import java.util.Map;
import java.util.Optional;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.springframework.web.util.JavaScriptUtils;

/**
 * Listener for Ajax request targets that updates the 'history', that is sets
 * the URL parameters based on the model values provided by the page IFF it
 * supports this by implementing {@link HistoryApiAware}.
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see HistoryApiAware
 */
public class HistoryApiAjaxRequestTargetListener extends AjaxRequestTarget.AbstractListener {

    @Override
    public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {
        super.onBeforeRespond(map, target);
        final Page page = target.getPage();
        if (page instanceof HistoryApiAware) {
            final Optional<String> script = createParamsScript((HistoryApiAware) page);
            if (script.isPresent()) {
                target.appendJavaScript(script.get());
            }
        }
    }

    private Optional<String> createParamsScript(HistoryApiAware page) {
        final StringBuilder state = new StringBuilder();
        final PageParameters params = page.getHistoryApiPageParameters();
        params.getNamedKeys().forEach((key) -> {
            params.getValues(key).stream()
                    .filter((objectItem) -> !objectItem.isEmpty())
                    .forEach((objectItem) -> {
                        addObjectStringToState(state, key, objectItem.toString());
                    });
        });
        if (state.length() == 0) {
            //nothing to do
            return Optional.empty();
        } else {
            //create javascript to replace state via history API
            return Optional.of(""
                    + "var stateString ='" + JavaScriptUtils.javaScriptEscape(state.toString()) + "';"
                    + "var path = this.window.location.pathname;"
                    + "var queryParams = this.window.location.search;"
                    + "var newUrl = path + '?' + stateString;"
                    + "var stateObj = { app: 'vlo' };" //TODO: wrap state in JSON ?
                    + "history.replaceState(stateObj, 'page', newUrl);");
        }
    }

    private void addObjectStringToState(StringBuilder state, String key, String value) {
        if (state.length() != 0) {
            state.append("&");
        }
        final String encodedValue = UrlEncoder.QUERY_INSTANCE.encode(value, "UTF-8");
        state.append(key).append("=").append(encodedValue);
    }

}
