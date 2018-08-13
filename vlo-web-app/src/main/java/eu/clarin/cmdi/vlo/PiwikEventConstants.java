/*
 * Copyright (C) 2017 CLARIN
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
package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;

/**
 * Category and action names to be used in Piwik tracking calls
 *
 * @author twagoo
 * @see AjaxPiwikTrackingBehavior
 */
public class PiwikEventConstants {

    //Event categories
    public static final String PIWIK_EVENT_CATEGORY_FACET = "Facet";
    public static final String PIWIK_EVENT_CATEGORY_RECORDPAGE = "RecordPageTab";
    public static final String PIWIK_EVENT_CATEGORY_LRS = "LRS";
    public static final String PIWIK_EVENT_CATEGORY_USER_SATISFACTION = "UserSatisfaction";

    //Actions
    public static final String PIWIK_EVENT_ACTION_FACET_EXPANDCOLLAPSE = "ExpandCollapse";
    public static final String PIWIK_EVENT_ACTION_FACET_ALLVALUES = "AllValues";
    public static final String PIWIK_EVENT_ACTION_FACET_SELECT = "Select";
    public static final String PIWIK_EVENT_ACTION_FACET_UNSELECT = "Unselect";
    public static final String PIWIK_EVENT_ACTION_AVAILABILITY = "AvailabilitySelection";
    public static final String PIWIK_EVENT_ACTION_RECORDPAGE_TABSWITCH = "Switch";
    public static final String PIWIK_EVENT_ACTION_LRS_PROCESSRESOURCE = "ProcessResource";
    public static final String PIWIK_EVENT_ACTION_HIERARCHY_UP = "HierarchyNavigateUp";
    public static final String PIWIK_EVENT_ACTION_HIERARCHY_CHILD = "HierarchyToggleChild";

    //Page views
    public static final String PIWIK_PAGEVIEW_SEARCH = "Search page";

}
