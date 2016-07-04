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
package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import eu.clarin.cmdi.vlo.wicket.pages.VloBasePage;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 *
 * @author twagoo
 */
public final class JavaScriptResources {

    private final static JavaScriptResourceReference VLO_FRONT = new JavaScriptResourceReference(VloBasePage.class, "vlo-front.js");
    private final static JavaScriptResourceReference VLO_HEADER = new JavaScriptResourceReference(VloBasePage.class, "vlo-header.js");
    private final static JavaScriptResourceReference VLO_SYNTAX_HELP = new JavaScriptResourceReference(VloBasePage.class, "vlo-syntax-help.js");
    private final static JavaScriptResourceReference VLO_FACETS = new JavaScriptResourceReference(VloBasePage.class, "vlo-facets.js");
    private final static JavaScriptResourceReference SEARCH = new JavaScriptResourceReference(VloBasePage.class, "vlo-search.js");
    private final static JavaScriptResourceReference FIELDS_TABLE = new JavaScriptResourceReference(RecordPage.class, "vlo-fields-table.js");
    private final static CssResourceReference JQUERY_UI_CSS = new CssResourceReference(VloBasePage.class, "jquery-ui-1.11.4.custom/jquery-ui.min.css");
    private final static JavaScriptResourceReference JQUERY_WATERMARK = new JavaScriptResourceReference(VloBasePage.class, "jquery.watermark-3.1.4/jquery.watermark.min.js");
    private final static JavaScriptResourceReference HIGHLIGHT = new JavaScriptResourceReference(RecordPage.class, "jquery.highlight.js");

    public static JavaScriptResourceReference getVloFrontJS() {
        return VLO_FRONT;
    }

    public static JavaScriptResourceReference getVloHeaderJS() {
        return VLO_HEADER;
    }
    public static JavaScriptResourceReference getVloFacetsJS() {
        return VLO_FACETS;
    }
    
    public static CssResourceReference getJQueryUICSS() {
        return JQUERY_UI_CSS;
    }

    public static JavaScriptResourceReference getJQueryWatermarkJS() {
        return JQUERY_WATERMARK;
    }

    public static JavaScriptResourceReference getHighlightJS() {
        return HIGHLIGHT;
    }

    public static JavaScriptResourceReference getSyntaxHelpJS() {
        return VLO_SYNTAX_HELP;
    }
    
    public static JavaScriptResourceReference getSearchFormJS() {
        return SEARCH;
    }

    public static ResourceReference getFieldsTableJS() {
        return FIELDS_TABLE;
    }
    
}
