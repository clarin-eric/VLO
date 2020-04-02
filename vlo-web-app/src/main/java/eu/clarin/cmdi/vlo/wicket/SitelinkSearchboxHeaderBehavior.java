/*
 * Copyright (C) 2019 CLARIN
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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Structured data to provide a search box in a search engine's search results
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @see https://developers.google.com/search/docs/data-types/sitelinks-searchbox
 */
public class SitelinkSearchboxHeaderBehavior extends JsonLdHeaderBehavior {

    private final static String QUERY_SUFFIX = "?" + VloWebAppParameters.QUERY + "={search_term_string}";
    private final static PageParameters EMPTY_PAGE_PARAMETERS = new PageParameters();

    public SitelinkSearchboxHeaderBehavior() {
        super(createJsonModel());
    }

    private static IModel<String> createJsonModel() {
        return (() -> {
            final RequestCycle requestCycle = RequestCycle.get();
            final UrlRenderer urlRenderer = requestCycle.getUrlRenderer();

            final String baseUrl = urlRenderer.renderFullUrl(Url.parse(requestCycle.urlFor(VloWicketApplication.get().getHomePage(), EMPTY_PAGE_PARAMETERS)));
            final String searchTargetUrl = urlRenderer.renderFullUrl(Url.parse(requestCycle.urlFor(FacetedSearchPage.class, EMPTY_PAGE_PARAMETERS))) + QUERY_SUFFIX;
            return "{\n"
                    + "  \"@context\": \"https://schema.org\",\n"
                    + "  \"@type\": \"WebSite\",\n"
                    + "  \"url\": \"" + baseUrl + "\",\n"
                    + "  \"potentialAction\": {\n"
                    + "    \"@type\": \"SearchAction\",\n"
                    + "    \"target\": \"" + searchTargetUrl + "\",\n"
                    + "    \"query-input\": \"required name=search_term_string\"\n"
                    + "  }\n"
                    + "}\n";
        });
    }
//            try {
//
//                //        // Open a valid json(-ld) input file
////InputStream inputStream = new FileInputStream("input.json");
////// Read the file into an Object (The type of this object will be a List, Map, String, Boolean,
////// Number or null depending on the root object in the file).
////Object jsonObject = JsonUtils.fromInputStream(inputStream);
//                Map<String, String> jsonObject = ImmutableMap.<String, String>builder()
//                        .put("url", baseUrl)
//                        .build();
//// Create a context JSON map containing prefixes and definitions
//                Map context = new HashMap();
//// Customise context...
//// Create an instance of JsonLdOptions with the standard JSON-LD options
//                JsonLdOptions options = new JsonLdOptions();
//// Customise options...
//// Call whichever JSONLD function you want! (e.g. compact)
//                Object compact = JsonLdProcessor.compact(jsonObject, context, options);
//// Print out the result (or don't, it's your call!)
//                return JsonUtils.toPrettyString(compact);
//            } catch (JsonLdError | IOException ex) {
//                return "//error: " + ex.getMessage();
//            }
//
//      });
//    }

}
