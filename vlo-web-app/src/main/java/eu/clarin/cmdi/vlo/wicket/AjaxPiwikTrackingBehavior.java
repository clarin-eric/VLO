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
package eu.clarin.cmdi.vlo.wicket;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;

/**
 * Behavior that sends a tracking request to the Piwik tracker on an Ajax event.
 * By default, it registers on the "click" event. It assumes the JavaScript
 * tracker to be loaded (usually by means of the tracking code included in the
 * parent page).
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AjaxPiwikTrackingBehavior extends AjaxEventBehavior {

    public static final String DEFAULT_EVENT = "click";
    public static final String TRACKER_VARIABLE_NAME = "tracker";

    protected AjaxPiwikTrackingBehavior(String event) {
        super(event);
    }

    @Override
    protected void onEvent(AjaxRequestTarget target) {
        target.appendJavaScript(String.format(
                "var %s = Piwik.getAsyncTracker(); %s", 
                TRACKER_VARIABLE_NAME, 
                getTrackerCommand(TRACKER_VARIABLE_NAME)));
    }

    protected abstract String getTrackerCommand(String trackerName);

    /**
     * Tracking of an action with a custom name
     *
     * @param event
     * @param actionTitle
     * @return
     */
    public static Behavior newEventTrackingBehavior(String event, final String actionTitle) {
        return new AjaxPiwikTrackingBehavior(event) {

            @Override
            protected String getTrackerCommand(String trackerName) {
                return trackerName + ".trackPageView('" + actionTitle + "');";
            }

        };
    }

    public static Behavior newEventTrackingBehavior(final String actionTitle) {
        return newEventTrackingBehavior(DEFAULT_EVENT, actionTitle);
    }
}
