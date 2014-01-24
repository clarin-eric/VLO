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

$(document).ready(function() {
    /* collection selection */
    $("li.collection").hide();
    $(".allcollections input").change(function(event) {
        if ($(this).is(":checked")) {
            $("li.collection").hide();
            $("li.collection input").attr("checked", false);
        } else {
            $("li.collection").show();
        }
    });
    $("a#showcollections").click(function(event) {
        $("li.collection").toggle();
    });
    $("li.collection input").change(function(event) {
        if ($(this).is(":checked")) {
            $(".allcollections input").attr("checked", false);
        }
    });

});
