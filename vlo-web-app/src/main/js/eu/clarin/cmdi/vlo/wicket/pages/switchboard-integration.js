/* 
 * Copyright (C) 2022 CLARIN
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


function handleTestSwitchboardMatches(data) {
    if (data.timeout) {
        console.error('testSwitchboardMatches: timeout');
        return;
    }
    console.log('testSwitchboardMatches: data retrieved', data);
    const element = $('.resourceDropdownSwitchboardItem a span', dropDown);
//            if (data.matches === 0)
//                element.setAttribute('class', 'disabled');
    let text = 'Process with Switchboard ';
    if (data.matches === 0) {
        text += '(no matches)';
    } else if (data.matches === 1) {
        text += '(one match)';
    } else {
        text += `(${data.matches} matches)`;
    }
    console.log('Text: ', text);
    element.text(text);
}

function handleTestSwitchboardMatchesError(e) {
    console.error('testSwitchboardMatches: failed', e);
}

function switchboardPreflight(link, dropdownId) {
    console.debug('switchboardPreflight', link, dropdownId);

    const dropDown = $(dropdownId);
    if (dropDown.length === 0) {
        console.error('Element not found: ', dropdownId);
        return;
    }

    if (typeof testSwitchboardMatches === 'function') {
        testSwitchboardMatches([link])
                .then(handleTestSwitchboardMatches)
                .catch(handleTestSwitchboardMatchesError);
    } else {
        console.error('testSwitchboardMatches function not found');
    }

}