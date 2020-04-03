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

import com.google.gson.annotations.SerializedName;
import eu.clarin.cmdi.vlo.VloWebAppParameters;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel;
import eu.clarin.cmdi.vlo.wicket.model.JsonLdModel.JsonLdObject;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
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

    private final static String QUERY_PLACEHOLDER = "search_term_string";
    private final static String QUERY_SUFFIX = "?" + VloWebAppParameters.QUERY + "={" + QUERY_PLACEHOLDER + "}";
    private final static PageParameters EMPTY_PAGE_PARAMETERS = new PageParameters();

    public SitelinkSearchboxHeaderBehavior() {
        super(createJsonModel());
    }

    private static IModel<String> createJsonModel() {
        final LoadableDetachableModel<JsonLdObject> objectModel = new LoadableDetachableModel<>() {
            @Override
            protected JsonLdObject load() {
                final RequestCycle requestCycle = RequestCycle.get();
                final UrlRenderer urlRenderer = requestCycle.getUrlRenderer();

                final String baseUrl = urlRenderer.renderFullUrl(Url.parse(requestCycle.urlFor(VloWicketApplication.get().getHomePage(), EMPTY_PAGE_PARAMETERS)));
                final String searchTargetUrl = urlRenderer.renderFullUrl(Url.parse(requestCycle.urlFor(FacetedSearchPage.class, EMPTY_PAGE_PARAMETERS))) + QUERY_SUFFIX;

                return new WebSite(baseUrl, new SearchAction(searchTargetUrl, "required name=" + QUERY_PLACEHOLDER));
            }

        };

        return new JsonLdModel(objectModel);

    }

    private static class SearchAction extends JsonLdObject {

        private final String target;

        @SerializedName("query-input")
        private final String queryInput;

        public SearchAction(String target, String queryInput) {
            super(null, "SearchAction");
            this.target = target;
            this.queryInput = queryInput;
        }

        public String getTarget() {
            return target;
        }

        public String getQueryInput() {
            return queryInput;
        }

    }

    private static class WebSite extends JsonLdObject {

        private final String url;

        private final SearchAction potentialAction;

        public WebSite(String url, SearchAction potentialAction) {
            super("https://schema.org", "WebSite");
            this.url = url;
            this.potentialAction = potentialAction;
        }

        public SearchAction getPotentialAction() {
            return potentialAction;
        }

        public String getUrl() {
            return url;
        }

    }

}
