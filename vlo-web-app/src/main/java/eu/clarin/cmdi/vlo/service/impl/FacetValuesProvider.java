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
package eu.clarin.cmdi.vlo.service.impl;

import eu.clarin.cmdi.vlo.service.FacetValuesService;
import eu.clarin.cmdi.vlo.pojo.Facet;
import eu.clarin.cmdi.vlo.pojo.FacetStatus;
import eu.clarin.cmdi.vlo.pojo.FacetValue;
import eu.clarin.cmdi.vlo.service.impl.FacetValuesProvider.FacetValuesSortProperty;
import java.util.Iterator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author twagoo
 */
public class FacetValuesProvider extends SortableDataProvider<FacetValue, FacetValuesSortProperty> {

    public enum FacetValuesSortProperty {

        NAME,
        COUNT
    }
    private final FacetValuesService fvService;
    private final IModel<FacetStatus> status;
    private final String filter;

    public FacetValuesProvider(FacetValuesService fvService, IModel<FacetStatus> status, String filter) {
        this.fvService = fvService;
        this.status = status;
        this.filter = filter;
    }

    @Override
    public Iterator<? extends FacetValue> iterator(long first, long count) {
        return fvService.getValues(
                status.getObject().getSelection().getFacet(),
                status.getObject().getContext(),
                filter,
                FacetValuesSortProperty.NAME).listIterator((int) first);
    }

    @Override
    public long size() {
        return fvService.getValueCount(
                status.getObject().getSelection().getFacet(),
                status.getObject().getContext(),
                filter);
    }

    @Override
    public IModel<FacetValue> model(FacetValue object) {
        return new Model(object);
    }
}
