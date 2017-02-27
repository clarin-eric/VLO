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
package eu.clarin.cmdi.vlo.wicket.components;

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.PageParametersConverter;
import eu.clarin.cmdi.vlo.wicket.pages.FacetedSearchPage;
import java.util.Collections;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class FacetSelectLink extends Link {

    @SpringBean(name = "queryParametersConverter")
    private PageParametersConverter<QueryFacetsSelection> paramsConverter;

    private final IModel valueModel;
    private final IModel<String> facetNameModel;

    public FacetSelectLink(String id, IModel valueModel, IModel<String> facetNameModel) {
        super(id);
        this.valueModel = valueModel;
        this.facetNameModel = facetNameModel;
    }

    @Override
    public void onClick() {
        final FacetSelection facetSelection = new FacetSelection(FacetSelectionType.AND, Collections.singleton(valueModel.getObject().toString()));
        final QueryFacetsSelection selection = new QueryFacetsSelection(Collections.singletonMap(facetNameModel.getObject(), facetSelection));
        getRequestCycle().setResponsePage(FacetedSearchPage.class, paramsConverter.toParameters(selection));
    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (valueModel != null) {
            valueModel.detach();
        }
        if (facetNameModel != null) {
            facetNameModel.detach();
        }
    }

}
