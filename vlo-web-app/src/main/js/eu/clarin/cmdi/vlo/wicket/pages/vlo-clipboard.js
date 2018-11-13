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


var showCopiedTooltip = function (button) {
    // show a 'copied' tooltip on the button for a second

    var title = button.attr('title');

    button.attr('title', 'Copied!');
    button.tooltip({
        "placement": "bottom",
        "trigger": "manual"
    });
    button.on('shown.bs.tooltip', function (e) {
        //remove tooltip
        setTimeout(function () {
            button.attr('title', title);
            button.tooltip('destroy');
        }, 1000);
    });
    button.tooltip('show');

    //flash button
    button.toggleClass('btn-info');
    setTimeout(function () {
        button.toggleClass('btn-info');
    }, 500);
};

$(document).ready(function () {
    // general handling for clipboard buttons
    var clipboard = new ClipboardJS('.btn.clipboard');
    clipboard.on('success', function (e) {
        // success feedback
        showCopiedTooltip($(e.trigger));
        
        //clear selection
        e.clearSelection();
    });

    // top navigation 'copy page link' option in 'share' dropdown
    $('#topnavigation').on('click', '.clipboard-copy-link', function (e) {
        // do not follow wicket link!
        e.preventDefault();
    });

    var clipboardPageLink = new ClipboardJS('#topnavigation .clipboard-copy-link');
    clipboardPageLink.on('success', function (e) {
        // success feedback
        showCopiedTooltip($(e.trigger).parents('.dropdown').children('.btn'));
    });

    clipboardPageLink.on('error', function (e) {
        // follow wicket link in case of failure
        console.log("Failed to copy link to clipboard");
        window.location = $(e.trigger).attr('href');
    });
});
