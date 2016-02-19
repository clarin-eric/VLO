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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behavior that sends a tracking request to the Piwik tracker on an Ajax event.
 * By default, it registers on the "click" event. It assumes the JavaScript
 * tracker to be loaded (usually by means of the tracking code included in the
 * parent page).
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class AjaxPiwikTrackingBehavior extends AjaxEventBehavior {

    public static final Logger log = LoggerFactory.getLogger(AjaxPiwikTrackingBehavior.class);
    
    public static final String DEFAULT_EVENT = "click";
    private final String trackerCommand;

    protected AjaxPiwikTrackingBehavior(String event, String trackerCommand) {
        super(event);
        this.trackerCommand = trackerCommand;
    }

    @Override
    protected void onEvent(AjaxRequestTarget target) {
        final String js = "if(Piwik != null) { "
                + "var tracker = Piwik.getAsyncTracker(); if(tracker != null) { tracker." + getTrackerCommand(target)
                + ";}}";
        log.debug("Calling Piwik API: {}", js);
        target.appendJavaScript(js);
    }

    protected String getTrackerCommand(AjaxRequestTarget target) {
        return trackerCommand;
    }

    /**
     * Tracking of an action with a custom name
     *
     * @param event
     * @param actionTitle
     * @return
     */
    public static Behavior newEventTrackingBehavior(String event, final String actionTitle) {
        return new AjaxPiwikTrackingBehavior(event, "trackPageView('" + actionTitle + "')");
    }

    public static Behavior newEventTrackingBehavior(final String actionTitle) {
        return newEventTrackingBehavior(DEFAULT_EVENT, actionTitle);
    }

    public static abstract class SearchTrackingBehavior extends AjaxPiwikTrackingBehavior {

        public SearchTrackingBehavior(String event) {
            super(event, null);
        }

        @Override
        protected String getTrackerCommand(AjaxRequestTarget target) {
            return "trackSiteSearch('" + getKeywords(target) + "')";
        }

        protected abstract String getKeywords(AjaxRequestTarget target);

    }
}
