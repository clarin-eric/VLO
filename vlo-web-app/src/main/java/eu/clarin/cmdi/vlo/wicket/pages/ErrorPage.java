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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ErrorPage extends VloBasePage<QueryFacetsSelection> {
            public final static String PAGE_PARAMETER_RESPONSE_CODE = "code";

    private final int responseCode;
    
    public ErrorPage(IModel<QueryFacetsSelection> model, int responseCode) {
        super(model);
        this.responseCode = responseCode;
    }
    
    public ErrorPage(PageParameters parameters) {
        super(parameters);
        this.responseCode = parameters.get(PAGE_PARAMETER_RESPONSE_CODE).toInt(500);
        //TODO: convert params to query facet selection
    }

    @Override
    protected void configureResponse(WebResponse response) {
        super.configureResponse(response);
        response.setStatus(responseCode);
    }
    
    @Override
    public boolean isVersioned() {
        return false;
    }
 
    @Override
    public boolean isErrorPage() {
        return true;
    }
    
}
