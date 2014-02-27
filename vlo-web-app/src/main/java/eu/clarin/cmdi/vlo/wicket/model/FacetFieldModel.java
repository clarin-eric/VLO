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
import eu.clarin.cmdi.vlo.service.FacetFieldsService;
import java.util.Collections;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author twagoo
 */
public class FacetFieldModel extends AbstractReadOnlyModel<FacetField> {

    //todo: can be made more efficient/elegant than wrapping fields model
    private final FacetFieldsModel fieldsModel;

    public FacetFieldModel(FacetFieldsService service, String facet, IModel<QueryFacetsSelection> selectionModel) {
        fieldsModel = new FacetFieldsModel(service, Collections.singletonList(facet), selectionModel);
    }

    @Override
    public FacetField getObject() {
        final List<FacetField> fieldsList = fieldsModel.getObject();
        if (fieldsList == null || fieldsList.isEmpty()) {
            return null;
        } else {
            return fieldsList.get(0);
        }
    }

}
