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


function registerRemoveHandler() {
    $(".removekeyword").click(function(event){
        event.preventDefault();
        $(this).parent().remove();
    });
}

$(document).ready(function() {
    $("#addkeyword").click(function(event) {
        event.preventDefault();
        $('<p wicket:id="keyword"><input type="text" name="keyword" value="" /><a href="#" class="removekeyword">Remove</a></p>')
                .appendTo($("#keywordswrapper"));
        // register remove handler for newly added remove link
        registerRemoveHandler();
    });
    registerRemoveHandler();
});

