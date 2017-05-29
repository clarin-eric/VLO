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
package eu.clarin.cmdi.vlo.wicket.model;

import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import java.util.Collections;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * Decorator for {@link FacetFieldsModel} for a selection of a single facet.
 *
 * Notice that the actual retrieval is carried out by the provided
 * {@link FacetFieldsService}, which therefore should be configured to actually
 * retrieve the specified facet (through the constructor), otherwise it may not
 * be presented.
 *
 * TODO: Provide some kind of batch retrieval and look up to prevent a call for
 * each instance of this
 *
 * @author twagoo
 */
public class FacetFieldModel extends AbstractReadOnlyModel<FacetField> {

    private final FacetFieldsModel fieldsModel;
    private final String facetName;

    /**
     *
     * @param service service to use for facet field retrieval
     * @param facet facet to provide
     * @param selectionModel model that provides current query/selection
     */
    public FacetFieldModel(String facet, FacetFieldsService service, IModel<QueryFacetsSelection> selectionModel) {
        this(facet, new FacetFieldsModel(service, Collections.singletonList(facet), selectionModel, -1));
    }

    public FacetFieldModel(String facetName, FacetFieldsModel fieldsModel) {
        this.fieldsModel = fieldsModel;
        this.facetName = facetName;
    }

    @Override
    public FacetField getObject() {
        return fieldsModel.getFacetField(facetName);
    }

    @Override
    public void detach() {
        fieldsModel.detach();
    }

}
