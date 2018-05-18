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

function applyPageParametersToHistory(stateString) {
    var query = this.window.location.search;
    if(query) {
        //keep wicket page id - append '&' IFF we know a state string will be appended
        var newQuery = query.replace(/\?(\d+)(&.*)?/, ((stateString)?'?$1&':'?$1'));
    } else {
        var newQuery = '?';
    }
    
    var path = this.window.location.pathname;
    var newUrl = path + newQuery + stateString;
    
    var stateObj = {app: 'vlo'}; //TODO: wrap state in JSON ?
    history.replaceState(stateObj, 'page', newUrl);
}