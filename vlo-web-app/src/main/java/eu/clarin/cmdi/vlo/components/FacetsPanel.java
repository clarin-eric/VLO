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
package eu.clarin.cmdi.vlo.components;

import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import eu.clarin.cmdi.vlo.service.impl.FacetFieldsDataProvider;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 *
 * @author twagoo
 */
public class FacetsPanel extends Panel {

    @SpringBean
    private FacetFieldsService facetFieldsService;

    public FacetsPanel(String id, IModel<QueryFacetsSelection> model) {
        super(id, model);

        final IDataProvider<FacetField> provider = new FacetFieldsDataProvider(facetFieldsService, 
                new PropertyModel<List<FacetSelection>>(model, "selection"),
                new PropertyModel<String>(model, "query"));
        add(new DataView<FacetField>("facets", provider) {

            @Override
            protected void populateItem(Item<FacetField> item) {
                item.add(new FacetPanel("facet", item.getModel()));
            }
        });
    }
}
