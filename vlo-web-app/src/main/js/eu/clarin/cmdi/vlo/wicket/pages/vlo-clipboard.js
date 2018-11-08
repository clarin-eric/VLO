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


$(document).ready(function () {
    var clipboard = new ClipboardJS('.btn.clipboard');
    clipboard.on('success', function (e) {
        var button = $(e.trigger);
        var title = button.attr('title');

        button.attr('title', 'Copied!');
        button.tooltip({
            "placement": "bottom",
            "trigger": "manual"
        });
        button.on('shown.bs.tooltip', function (e) {
            setTimeout(function () {
                button.attr('title', title);
                button.tooltip('destroy');
            }, 1000);
        });
        button.tooltip('show');
    });
});
