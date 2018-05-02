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
import org.apache.wicket.model.IModel;

/**
 * Pages must implement this interface in order to support URL rewriting based
 * on the HTML 5 History API
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see HistoryApiAjaxRequestTargetListener
 */
public interface HistoryApiAware {

    /**
     *
     * @return map of parameter names and value models to represent as query
     * parameters {@literal e.g.}
     * {@code ?{key1}={model value1}&{key2}={model value 2}}
     */
    Map<String, IModel> getUrlParametersMap();
}
