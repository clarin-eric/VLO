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

//function expandFacet(p) {
//    p.find(".facetvalues").show(200);
//    p.addClass("expandedfacet");
//    p.removeClass("collapsedfacet");
//    p.find(".filterform input").focus();
//}
//
//function collapseFacet(p) {
//    p.find(".facetvalues").hide(200);
//    p.addClass("collapsedfacet");
//    p.removeClass("expandedfacet");
//}
//
//$(document).ready(function() {
//    /* facet collapse/expand */
//    $("a.expandfacet, a#showvalues").click(function(event) {
//        event.preventDefault();
//        var p = $(this).parents(".collapsedfacet");
//        expandFacet(p);
//    });
//    $("a.collapsefacet").click(function(event) {
//        event.preventDefault();
//        var p = $(this).parents(".expandedfacet");
//        collapseFacet(p);
//    });
//
//    $(".facet h1 a").click(function(event) {
//        event.preventDefault();
//        if ($(this).parents(".expandedfacet").length === 1) {
//            var p = $(this).parents(".expandedfacet");
//            collapseFacet(p);
//        } else {
//            var p = $(this).parents(".collapsedfacet");
//            expandFacet(p);
//        }
//    });
//
//    /* facet filter */
////    $(".filterform").hide();
////
////    $("a.filtertoggle").click(function(event) {
////        // toggle link clicked, show or hide filter box and focus on input
////        event.preventDefault();
////        var form = $(this).parent(".facet").find(".filterform");
////        form.siblings(".facetvalues").find("li").show();
////        expandFacet($(this).parent(".collapsedfacet"));
////        form.toggle(100, function(event) {
////            var input = form.children("input");
////            input.val('');
////            input.focus();
////        });
////    });
//
//    var filterHandler = function(event) {
//        // filter text entered, update result list
//        var links = $(this).parents(".filterform").siblings(".facetvalues").find("li");
//        var match = $(this).val().toUpperCase();
//        if (match.length === 0) {
//            links.show();
//        } else {
//            // hide all results
//            links.hide();
//            // show all matching results
//            links.filter(function(index) {
//                // case insensitive match
//                return links[index].textContent.toUpperCase().indexOf(match) >= 0;
//            }).show();
//        }
//    };
//
//    $(".filterform input").on('input', filterHandler);
//
//    /* Facet values popup */
//
//    $(".more-link").click(function(event) {
//        event.preventDefault();
//        $("#facetvalues").toggle();
//        var name = $(this).parents(".facet").find("h1").text();
//        $("#facetvalues h2").text(name);
//        $(window).scrollTop($('#facetvalues').position().top);
//    });
//
//    $("#facetvalues a").click(function(event) {
//        event.preventDefault();
//        $("#facetvalues").toggle();
//    });
//});
