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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import eu.clarin.cmdi.vlo.VloWicketApplication;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.service.solr.FacetFieldsService;
import java.util.Collection;
import java.util.List;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * Model that provides a list of {@link FacetField}s based on the current query
 * and values selection, filtered through a selection of facet names.
 *
 * Notice that the actual retrieval is carried out by the provided
 * {@link FacetFieldsService}, which therefore should be configured to actually
 * retrieve the specified facets (in the list in the constructor), otherwise
 * some of these may not be present.
 *
 * @author twagoo
 */
public class FacetFieldsModel extends LoadableDetachableModel<List<FacetField>> {

    private final FacetFieldsService service;
    private final List<String> facets;
    private final IModel<QueryFacetsSelection> selectionModel;

    public FacetFieldsModel(List<String> facets, IModel<QueryFacetsSelection> selectionModel) {
        this(VloWicketApplication.get().getFacetFieldsService(), facets, selectionModel);
    }

    /**
     *
     * @param service service to use for facet field retrieval
     * @param facets facets to include
     * @param selectionModel model that provides current query/selection
     */
    protected FacetFieldsModel(FacetFieldsService service, List<String> facets, IModel<QueryFacetsSelection> selectionModel) {
        this.service = service;
        this.facets = facets;
        this.selectionModel = selectionModel;
    }

    @Override
    protected List<FacetField> load() {
        final List<FacetField> allFacetFields = service.getFacetFields(selectionModel.getObject());
        final Collection<FacetField> filtered = Collections2.filter(allFacetFields, new Predicate<FacetField>() {

            @Override
            public boolean apply(FacetField t) {
                return facets.contains(t.getName());
            }
        });
        return ImmutableList.copyOf(filtered);
    }

    @Override
    public void detach() {
        super.detach();
        selectionModel.detach();
    }

}
