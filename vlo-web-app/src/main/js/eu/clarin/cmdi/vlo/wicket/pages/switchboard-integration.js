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

const switchboardConfig = {
    'preflightTimeout': 5000
};

$(document).ready(function () {
    if (typeof setSwitchboardConfig === 'function') {
        console.debug('switchboardConfig: ', switchboardConfig);
        setSwitchboardConfig(switchboardConfig);
    } else {
        console.warn("Could not configure switchboard, 'setSwitchboardConfig' function not found");
    }
});

function handleTestSwitchboardMatches(element, baseText, data) {
    if (data.timeout) {
        console.error('testSwitchboardMatches: timeout');
        return;
    }
    console.debug('testSwitchboardMatches: data retrieved', data);
    let text = baseText;
    if (data.matches === 0) {
        text += ' (no matches)';
    } else if (data.matches === 1) {
        text += ' (one match)';
    } else {
        text += ' (' + data.matches + ' matches)';
    }

    console.log('Text: ', text);
    element.text(text);
}

function handleTestSwitchboardMatchesError(e) {
    console.error('testSwitchboardMatches: failed', e);
}

function switchboardPreflight(link, item, baseText) {
    if (typeof testSwitchboardMatches === 'function') {
        testSwitchboardMatches([link])
                .then(handleTestSwitchboardMatches.bind(null, item, baseText))
                .catch(handleTestSwitchboardMatchesError);
    } else {
        console.error('testSwitchboardMatches function not found');
    }
}

const baseTextMap = {};

function getBaseText(id, element) {
    if (baseTextMap[id] === undefined) {
        baseTextMap[id] = element.text();
    }
    return baseTextMap[id];
}

function registerSwitchboardPreflight(uri, dropdownSelector) {
    console.debug('Registering switchboard preflight handler on dropdown shown event for ', dropdownSelector);

    $(document).on('shown.bs.dropdown', dropdownSelector, function () {
        console.debug('switchboardPreflight', uri, dropdownSelector);
        const selector = dropdownSelector + ' a.resourceDropdownSwitchboardItem span';
        const item = $(selector);
        if (item.length === 0) {
            console.error('Element not found: ', selector);
            return;
        }

        switchboardPreflight(uri, item, getBaseText(dropdownSelector, item));
    });
}
