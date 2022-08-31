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

import com.google.common.collect.ImmutableMap;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.wicket.historyapi.HistoryApiAjaxRequestTargetListener;
import eu.clarin.cmdi.vlo.wicket.pages.RecordPage;
import eu.clarin.cmdi.vlo.wicket.pages.VloBasePage;
import java.util.Map;
import javax.inject.Inject;
import org.apache.wicket.request.resource.ContextRelativeResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.TextTemplateResourceReference;

/**
 *
 * @author twagoo
 */
public class JavaScriptResources {

    private final static ResourceReference BOOTSTRAP = new ContextRelativeResourceReference("script/bootstrap.js"); //bootstrap scripts are extracted from CLARIN's base style bootstrap package
    private final static JavaScriptResourceReference HIGHLIGHT = new JavaScriptResourceReference(RecordPage.class, "jquery.highlight.js");

    private final static JavaScriptResourceReference VLO_FRONT = new JavaScriptResourceReference(VloBasePage.class, "vlo-front.js");
    private final static JavaScriptResourceReference VLO_HEADER = new JavaScriptResourceReference(VloBasePage.class, "vlo-header.js");
    private final static JavaScriptResourceReference VLO_SYNTAX_HELP = new JavaScriptResourceReference(VloBasePage.class, "vlo-syntax-help.js");
    private final static JavaScriptResourceReference VLO_FACETS = new JavaScriptResourceReference(VloBasePage.class, "vlo-facets.js");
    private final static JavaScriptResourceReference VLO_CLIPBOARD = new JavaScriptResourceReference(VloBasePage.class, "vlo-clipboard.js");
    private final static JavaScriptResourceReference SEARCH = new JavaScriptResourceReference(VloBasePage.class, "vlo-search.js");
    private final static JavaScriptResourceReference FIELDS_TABLE = new JavaScriptResourceReference(RecordPage.class, "vlo-fields-table.js");
    private final static JavaScriptResourceReference HISTORY_API = new JavaScriptResourceReference(HistoryApiAjaxRequestTargetListener.class, "vlo-historyapi.js");

    private final static ResourceReference VCR_PLUGIN_JS = new ContextRelativeResourceReference("assets/vcr-plugin/dist/vcr-integration.js");

    private final static ResourceReference BOOTSTRAP_TOC = new ContextRelativeResourceReference("assets/bootstrap-toc/bootstrap-toc.js");
    private final static ResourceReference BOOTSTRAP_TOUR = new ContextRelativeResourceReference("assets/bootstrap-tour/bootstrap-tourist.js", false);

    private final static ResourceReference CLIPBOARD_JS = new ContextRelativeResourceReference("assets/clipboardjs/dist/clipboard.js");

    private Map<String, Object> configMap;

    public JavaScriptResources() {

    }

    public JavaScriptResources(VloConfig config) {
        this.setConfig(config);
    }

    public final void setConfig(VloConfig config) {
        //TODO: is there a nicer way to turn the entire configuration object into a Map<String,Object> ??
        configMap = ImmutableMap.<String, Object>builder()
                .put("vcrSubmitEndpoint", config.getVcrSubmitEndpoint())
                .build();
    }

    public ResourceReference getBootstrapJS() {
        return BOOTSTRAP;
    }

    public JavaScriptResourceReference getVloFrontJS() {
        return VLO_FRONT;
    }

    public JavaScriptResourceReference getVloHeaderJS() {
        return VLO_HEADER;
    }

    public JavaScriptResourceReference getHistoryApiJS() {
        return HISTORY_API;
    }

    public static JavaScriptResourceReference getVloFacetsJS() {
        return VLO_FACETS;
    }

    public JavaScriptResourceReference getHighlightJS() {
        return HIGHLIGHT;
    }

    public JavaScriptResourceReference getSyntaxHelpJS() {
        return VLO_SYNTAX_HELP;
    }

    public JavaScriptResourceReference getSearchFormJS() {
        return SEARCH;
    }

    public JavaScriptResourceReference getVloClipboardJS() {
        return VLO_CLIPBOARD;
    }

    public ResourceReference getFieldsTableJS() {
        return FIELDS_TABLE;
    }

    public ResourceReference getVcrPluginConfigJS() {
        return new TextTemplateResourceReference(VloBasePage.class, "vcr-integration-config.js", "text/javascript", "UTF-8", () -> {
            return configMap;
        });
    }

    public ResourceReference getVcrPluginJS() {
        return VCR_PLUGIN_JS;
    }

    public ResourceReference getBootstrapToc() {
        return BOOTSTRAP_TOC;
    }

    public ResourceReference getBootstrapTour() {
        return BOOTSTRAP_TOUR;
    }

    public ResourceReference getClipBoardJS() {
        return CLIPBOARD_JS;
    }
}
