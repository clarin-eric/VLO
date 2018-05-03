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
    
    if(restart) {
        tour.restart();
    }

    // Initialize the tour
    tour.init();
    
    if(step !== undefined) {
        tour.goTo(step);
    }

    // Start the tour
    return tour.start();
}

function createTourSteps() {
    return [
        {
            element: "#header",
            title: "VLO guided tour",
            content: "Would you like to get a quick tour of the VLO? This can also be triggered from the Help page.",
            placement: "auto bottom",
            onShown: function(tour) {
                $('#step-'+tour.getCurrentStep()+' button[data-role=prev]').hide();
                $('#step-'+tour.getCurrentStep()+' button[data-role=next]').text('Take tour');
                $('#step-'+tour.getCurrentStep()+' button[data-role=end]').text('No thanks');
            }
        },
        {
            element: "#search-form",
            title: "Search form",
            content: "Enter one or more keywords in the box to search through all records. You can use AND, OR and NOT to create more advanced queries. These and other options are explained in detail in the <a href='help'>Help page</a>.",
            placement: "auto bottom"
        }
    ];
    
}

$(document).ready(function() {
    if(window.location.hash === '#tour') {
        startVloTour(true, 1);
    } else {
        startVloTour(false);
    }
});
