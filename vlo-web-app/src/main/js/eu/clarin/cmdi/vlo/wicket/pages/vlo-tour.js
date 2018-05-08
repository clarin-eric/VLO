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

function startVloTour(restart, step) {
    var opts = {
        name: 'vlo-tour',
        steps: createTourSteps()
    };

    var tour = new Tour(opts);

    if (restart) {
        tour.restart();
    }

    // Initialize the tour
    tour.init();

    if (step !== undefined) {
        tour.goTo(step);
    }

    // Start the tour
    return tour.start();
}

function hideSimpleAndStart() {
    if (transitionFromSimple) {
        transitionFromSimple(function () {
            startVloTour(true);
        });
    } else {
        startVloTour(true);
    }
}

function typeValue(input, value) {
    if (value.length > 0) {
        var current = $(input).val();
        $(input).val(current + value.substr(0, 1));
        if (value.length > 1) {
            setTimeout(function () {
                typeValue(input, value.substr(1));
            }, 30 + Math.floor(50 * Math.random()));
        }
    }
}

function createTourSteps() {

    //  Steps from CLARIN-PLUS screencast (https://b2drop.eudat.eu/s/FrrnfN1refArxFy)
    //  
    //    * text search
    //    * results
    //    * facets ('categories')
    //    * expand a few
    //    * filter by language (e.g. estonian)
    //        * one result remains
    //    * search result
    //        * expand for details
    //        * availability laundry tags
    //    * navigate to record page
    //        * tabs
    //        * original provider
    //        * goes into repository
    //        * back to VLO
    //        * back to search results
    //    * remove selection
    //    * adapt search
    //        * resource type
    //    * help page    

    return [
        {
            element: "#search-form",
            title: "Enter query",
            content: "Enter one or more keywords in the search box to search through all records. You can use AND, OR and NOT to create more advanced queries. These and other options are explained in detail in the <a href='help#syntax'>Help page</a>.",
            placement: "auto top"
        }, {
            element: "#search-form button",
            title: "Search",
            content: "Press the button (or the enter key) to search",
            placement: "auto bottom",
            onShow: function () {
                var searchInput = $('#search-form input.search-box')
                if (searchInput.val() === '') {
                    typeValue(searchInput, 'speech');
                }
            },
            onShown: function (tour) {
                //listen to form submit - go to next if submitted
                $('#search-form .search-button').one('click', function () {
                    $('#search-form').data('tour-submitted', true);
                    tour.goTo(2);
                });
            }
        }, {
            element: "#searchresultitems",
            title: "Results",
            content: "Search results are shown here",
            placement: "auto top",
            onShow: function () {
                //submit search form if not submitted yet
                if (!$('#search-form').data('tour-submitted')) {
                    $('#search-form .search-button').click();
                }
            }
        }, {
            element: "#facets",
            title: "Facets",
            content: "Various 'facets' allow you to narrow down the search results.",
            placement: "auto right"
        }, {
            element: "#facets .facet:first",
            title: "Expand facet",
            content: "Click the name of a facet to expand it and see the values found within the current search results.",
            placement: "auto right"
        }, {
            element: "#facets .expandedfacet",
            title: "Facet values",
            content: "These are the values that occur within the search results for this facet. Click a value to select it and narrow down the search results accordingly. Try selecting one or more values and see how the result listing changes.",
            placement: "auto right",
            onShow: function () {
                //expand a facet if not expanded yet
                if ($('#facets .expandedfacet').length <= 0) {
                    $('#facets .facet:first a.expandable-panel-toggle').click();
                }
                return (new jQuery.Deferred()).promise();
            }
        }
    ];

}

$(document).ready(function () {
    if (window.location.hash === '#tour') {
        hideSimpleAndStart();
    } else {
        $("#simple-jumbotron #learn-more").hide();
        $("#simple-jumbotron #take-tour").removeClass('hidden');
        $("#simple-jumbotron #take-tour").click(function () {
            hideSimpleAndStart();
        });
    }

});
