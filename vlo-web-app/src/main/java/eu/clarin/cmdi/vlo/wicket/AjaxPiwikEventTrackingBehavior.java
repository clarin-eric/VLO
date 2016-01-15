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

/**
 * Behavior that sends an action to the Piwik tracker on an Ajax event. By
 * default, it registers on the "click" event. It assumes the JavaScript tracker
 * to be loaded (usually by means of the tracking code included in the parent
 * page).
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class AjaxPiwikEventTrackingBehavior extends AjaxEventBehavior {

    public static final String DEFAULT_EVENT = "click";
    private final String actionTitle;

    /**
     * Creates a tracking behavior registered on the default event
     *
     * @param actionTitle title of the action to track
     * @see #DEFAULT_EVENT
     */
    public AjaxPiwikEventTrackingBehavior(String actionTitle) {
        this(DEFAULT_EVENT, actionTitle);
    }

    /**
     * Creates a tracking behavior registered on the default event
     *
     * @param event name of the event to register on, e.g. "click" or "change"
     * @param actionTitle title of the action to track
     * @see AjaxEventBehavior#AjaxEventBehavior(java.lang.String)
     */
    public AjaxPiwikEventTrackingBehavior(String event, String actionTitle) {
        super(event);
        this.actionTitle = actionTitle;
    }

    @Override
    protected void onEvent(AjaxRequestTarget target) {
        target.appendJavaScript(""
                + "var tracker = Piwik.getAsyncTracker();"
                + "tracker.trackPageView('" + actionTitle + "');");
    }

}
