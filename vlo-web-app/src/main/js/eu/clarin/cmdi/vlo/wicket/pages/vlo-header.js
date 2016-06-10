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

function selectLinkText() {
    var input = document.getElementById('bookmarkLinkInput');
    if (input === null) {
        return false;
    } else {
        input.focus();
        input.setSelectionRange(0, input.value.length);
        return true;
    }
}

function copyLinkText() {
    if (selectLinkText()) {
        try {
            document.execCommand('copy');
            return true;
        } catch (e) {
            console.log("Failed to copy!");
            console.log(e);
        }
    } else {
        console.log("Failed to select text");
    }
    //if we get here either text selection or copy action went wrong
    alert('Could not copy to clipboard. Please copy the link manually.');
    return false;
}

function onModalShown() {
    //copy to clipboard button and instructions are initially hidden because 
    //this requires javascript and should not be shown unless JS is enabled
    $('.copy-to-clipboard').show();
    $('.js-instructions').show();
}
