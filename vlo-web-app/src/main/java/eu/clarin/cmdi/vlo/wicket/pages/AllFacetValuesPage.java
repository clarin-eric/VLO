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

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.model.SolrFieldNameModel;
import eu.clarin.cmdi.vlo.wicket.panels.AllFacetValuesPanel;
import eu.clarin.cmdi.vlo.wicket.panels.BreadCrumbPanel;
import java.util.Collection;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

/**
 *
 * @author twagoo
 */
public class AllFacetValuesPage extends VloBasePage<FacetField> {

    private final IModel<QueryFacetsSelection> selectionModel;

    public AllFacetValuesPage(IModel<FacetField> fieldModel, final IModel<QueryFacetsSelection> selectionModel) {
        super(fieldModel);
        this.selectionModel = selectionModel;

        add(new BreadCrumbPanel("breadcrumbs", selectionModel));
        
        add(new Label("name", new SolrFieldNameModel(new PropertyModel<String>(fieldModel, "name"))));

        add(new AllFacetValuesPanel("values", fieldModel) {

            @Override
            protected void onValuesSelected(String facet, Collection<String> values, AjaxRequestTarget target) {
                // Create updated selection state
                final QueryFacetsSelection newSelection;
                if (selectionModel != null) {
                    newSelection = selectionModel.getObject().getCopy();
                } else {
                    newSelection = new QueryFacetsSelection();
                }
                newSelection.selectValues(facet, values);

                // Redirect to search page with updated model
                final FacetedSearchPage searchPage = new FacetedSearchPage(Model.of(newSelection));
                setResponsePage(searchPage);
            }
        });
    }

    @Override
    public void detachModels() {
        super.detachModels();
        selectionModel.detach();
    }

}
