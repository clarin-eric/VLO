/* 
 * Copyright (C) 2016 CLARIN
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

// functions called by show/hide methods of BootstrapModal panel
// see https://getbootstrap.com/javascript/#modals-methods

function showModal(obj, cb) {
    if (cb) {
        obj.one('shown.bs.modal', function (e) {
            cb(); // wicket callback to proceed after showing the modal
        });
    }
    obj.modal({
        'backdrop': 'static', 
        'keyboard': false, 
        'show': true
    });
}

function hideModal(obj, cb) {
    if (cb) {
        obj.one('hidden.bs.modal', function (e) {
            cb(); // wicket callback to proceed after hiding the modal
        });
    }
    obj.modal('hide');
}

