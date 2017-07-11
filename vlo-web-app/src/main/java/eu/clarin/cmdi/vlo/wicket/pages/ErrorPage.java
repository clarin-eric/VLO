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
package eu.clarin.cmdi.vlo.wicket.pages;

import eu.clarin.cmdi.vlo.VloWebAppParameters;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author Twan Goosen &lt;twan@clarin.eu&gt;
 */
public class ErrorPage extends VloBasePage {

    public final static String PAGE_PARAMETER_RESPONSE_CODE = "code";

    private final ErrorType errorType;

    public ErrorPage(PageParameters parameters) {
        super(parameters);
        this.errorType = getErrorType(parameters.get(PAGE_PARAMETER_RESPONSE_CODE).toString());

        final PageParameters queryParams = new PageParameters(parameters).remove(PAGE_PARAMETER_RESPONSE_CODE);
        final boolean hasQuery = !queryParams.get(VloWebAppParameters.QUERY).isEmpty() || !queryParams.get(VloWebAppParameters.FILTER_QUERY).isEmpty();
        add(new BookmarkablePageLink("searchPage", FacetedSearchPage.class, queryParams)
                .add(new Label("label", hasQuery ? "Return to query" : "Go to the search page"))
        );

        final IModel<String> errorDescriptionModel = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return getDescriptionForError(errorType);
            }
        };
        add(new Label("description", errorDescriptionModel) {
            @Override
            protected void onConfigure() {
                setVisible(errorDescriptionModel.getObject() != null);
            }

        }.setEscapeModelStrings(false));
    }

    private String getDescriptionForError(ErrorType type) {
        switch (type) {
            case PAGE_NOT_FOUND:
                return "<p>The requested page does not exist in the VLO!</p>";
            case DOCUMENT_NOT_FOUND:
                return "<p>The requested record does not exist (anymore) in the VLO. "
                        + "The provider may have removed it from its repository or the identifier "
                        + "may have changed. Here's a number of things you may try to do:</p>"
                        + "<ul>"
                        + "<li>Try searching for the document by its name if you know it.</li>"
                        + "<li>Select the appropriate value from the 'collection' facet if "
                        + "you know what collection the record belongs to.</li>"
                        + "<li>If you think the record should be present in the VLO but cannot find it, please "
                        + "contact the maintainers of the VLO using the contact information in the 'About' page.</li>"
                        + "</ul>";
            case UNKNOWN_ERROR:
                return "<p>An unknown error has occurred! If this keeps happening, please notify the maintainers "
                        + "of the VLO using the contact information in the 'About' page.</p>";
            default:
                return null;
        }
    }

    @Override
    protected void configureResponse(WebResponse response) {
        super.configureResponse(response);
        response.setStatus(errorType.code());
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        //error page should not be indexed
        response.render(MetaDataHeaderItem.forMetaTag("robots", "noindex"));
    }

    public static enum ErrorType {
        PAGE_NOT_FOUND("page-not-found", HttpServletResponse.SC_NOT_FOUND),
        DOCUMENT_NOT_FOUND("document-not-found", HttpServletResponse.SC_NOT_FOUND),
        UNKNOWN_ERROR("unknown", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        private final String path;
        private final int code;

        private ErrorType(String path, int code) {
            this.path = path;
            this.code = code;
        }

        public String path() {
            return path;
        }

        public int code() {
            return code;
        }

    }

    public static void triggerErrorPage(ErrorType statusCode) {
        triggerErrorPage(statusCode, null);
    }

    public static void triggerErrorPage(ErrorType errorType, PageParameters pageParameters) {
        final PageParameters params;
        if (pageParameters == null) {
            params = new PageParameters();
        } else {
            params = new PageParameters(pageParameters);
        }

        throw new RestartResponseException(ErrorPage.class, params.add(PAGE_PARAMETER_RESPONSE_CODE, errorType.path()));
    }

    public static ErrorType getErrorType(String path) {
        for (ErrorType type : ErrorType.values()) {
            if (type.path().equals(path)) {
                return type;
            }
        }
        return ErrorType.UNKNOWN_ERROR;
    }

}
