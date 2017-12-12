/* 
 * Copyright (C) 2015 CLARIN
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

$(function () {
    //enable nicer bootstrap-style tooltip via plugin
    //http://getbootstrap.com/javascript/#tooltips
    $('form#search-form [data-toggle="tooltip"]').tooltip();
});

function startSearch() {
    //hide tooltip if shown
    $('form#search-form [data-toggle="tooltip"]').tooltip('hide');
    $('form#search-form').addClass('loading');
    $('form#search-form button').attr("disabled", "disabled");
}

function endSearch() {
    $('form#search-form').removeClass('loading');
    $('form#search-form button').removeAttr("disabled");
    //re-enable tooltip
    $('form#search-form [data-toggle="tooltip"]').tooltip();
}

function handleSearchFailure(message, status) {
    console.log("Search failure. Status: " + status + ", message:" + message);
    endSearch();
}

function transitionFromSimple(cb) {
    var simpleBox = $('.simple-only:visible');
    if(simpleBox.length > 0) {
        simpleBox.slideUp({
            duration: 'fast',
            start: function () {
                console.log("transition animation..");
                $('.hide-simple').slideDown('fast');
            },
            done: function () {
                cb();
            }
        });
    } else {
        cb();
    }
}

function showSearchContent() {
    $("#simple-filler").hide();
    $('.hide-simple').fadeIn();
}

function hideSimple() {
    var scrollpos = $(window).scrollTop();
    //subtract height of elements to be hidden from scroll position
    $(".simple-only, .jumbotron").each(function () {
        scrollpos -= $(this).height();
    });

    //perform hide and set scroll position when done
    $("#topnavigation").show(0, function () {
        scrollpos += $("#topnavigation").height();
        $(".simple-only, .jumbotron").hide(0, function () {
            window.scrollTo(0, scrollpos);
        });
    });
}

function ensureFillerSize() {
    var simplefiller = $(".simple #simple-filler");
    if (simplefiller.length > 0 && simplefiller.is(":visible")) {
        var fillerHeight = 50 + $(window).height() - simplefiller.offset().top - $("footer").height();
        simplefiller.height(Math.max(150, fillerHeight));
    }
}

$(document).ready(function () {
    //prepare simple mode (if faceted search is in fact in simple mode)
    if ($("#faceted-search.simple").length > 0) { // check whether we have .simple

        //make sure that there is always room to scroll
        ensureFillerSize();
        $(window).resize(ensureFillerSize);

        //top navigation will be shown again once we have switched out of simple mode
        $(".simple #topnavigation").hide(0);

        //subtle scroll hint
        $(".simple #simple-filler p").fadeIn(10000);

        //store last scroll position
        var lastScrollTop = 0;
        //show non-simple contents when user scrolls to its area
        $(window).scroll(function () {
            var st = $(this).scrollTop();
            var filler = $(".simple #simple-filler");
            if (filler.is(":visible")) { // filler is only visible if the user has not scrolled yet
                var windowBottom = $(window).scrollTop() + $(window).height();
                var fillerPos = filler.offset().top;
                if (windowBottom > fillerPos) {
                    showSearchContent();
                }
            } else {
                if (st < lastScrollTop //on scroll up
                        && $(".simple .jumbotron").is(":visible")) { // and if jumbotron still visible
                    //hide simple (mainly jumbotron) if scrolled beyond content top and scrolling up again
                    var contentPos = $('#search-content').offset().top;
                    if (st > contentPos) {
                        hideSimple();
                    }

                }
            }
            lastScrollTop = st;
        });

        //handle 'show all' button
        $(".simple a.switch-from-simple").click(function (evt) {
            evt.preventDefault();
            showSearchContent();
            $('body').animate({
                scrollTop: $("#search-content").offset().top - 20,
            }, 1000
                    );
        });
    }
});
