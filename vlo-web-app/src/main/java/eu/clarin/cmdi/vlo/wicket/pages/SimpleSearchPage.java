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
package eu.clarin.cmdi.vlo.wicket.pages;

import static eu.clarin.cmdi.vlo.VloWebAppParameters.DOCUMENT_ID;
import static eu.clarin.cmdi.vlo.VloWebAppParameters.FILTER_QUERY;
import static eu.clarin.cmdi.vlo.VloWebAppParameters.QUERY;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import java.util.Set;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;

/**
 * Extension of faceted search page with initial 'simple mode' model set to true
 *
 * @author twagoo
 */
public class SimpleSearchPage extends FacetedSearchPage {

    public SimpleSearchPage(IModel<QueryFacetsSelection> queryModel) {
        super(queryModel, Model.of(true));
        if (!Strings.isEmpty(queryModel.getObject().getQuery()) || !queryModel.getObject().getSelection().isEmpty()) {
            //query selection -> redirect to non-simple page
            setResponsePage(new FacetedSearchPage(queryModel));
        }
    }

    public SimpleSearchPage(PageParameters parameters) {
        super(parameters, Model.of(true));
        
        //see if another page would be more suitable
        final Set<String> keys = parameters.getNamedKeys();
        if (keys.contains(DOCUMENT_ID)) {
            //document id -> redirect to record page
            setResponsePage(new RecordPage(parameters));
        } else if (keys.contains(QUERY) || keys.contains(FILTER_QUERY)) {
            //query or selection parameters -> redirect to non-simple page
            setResponsePage(new FacetedSearchPage(parameters));
        }
    }

}
